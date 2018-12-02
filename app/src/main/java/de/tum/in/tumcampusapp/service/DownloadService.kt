package de.tum.`in`.tumcampusapp.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.UploadStatus
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Location
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsController
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.CacheManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.doAsync
import java.io.IOException

/**
 * Service used to download files from external pages
 */
class DownloadService : JobIntentService() {

    private val broadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    private val tumCabeClient: TUMCabeClient by lazy {
        TUMCabeClient.getInstance(this)
    }

    private val database: TcaDb by lazy {
        TcaDb.getInstance(this)
    }

    private val topNewsRemoteRepository: TopNewsRemoteRepository by lazy {
        TopNewsRemoteRepository(this, tumCabeClient)
    }

    private val kinoRemoteRepository: KinoRemoteRepository by lazy {
        val localRepo = KinoLocalRepository(database)
        KinoRemoteRepository(tumCabeClient, localRepo)
    }

    private val cafeteriaRemoteRepository: CafeteriaRemoteRepository by lazy {
        val localRepo = CafeteriaLocalRepository(database)
        CafeteriaRemoteRepository(tumCabeClient, localRepo)
    }

    private val disposable = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        Utils.log("DownloadService service has started")
    }

    override fun onHandleWork(intent: Intent) {
        doAsync {
            download(intent, this@DownloadService)
        }
    }

    private fun broadcastDownloadSuccess() {
        sendServiceBroadcast(Const.COMPLETED, null)
    }

    private fun broadcastDownloadError(messageResId: Int) {
        sendServiceBroadcast(Const.ERROR, messageResId)
    }

    private fun sendServiceBroadcast(action: String, messageResId: Int?) {
        val intent = Intent(BROADCAST_NAME).apply {
            putExtra(Const.ACTION_EXTRA, action)
            putExtra(Const.MESSAGE, messageResId)
        }
        broadcastManager.sendBroadcast(intent)
    }

    /**
     * Download all external data and returns whether the download was successful
     *
     * @param force True to force download over normal sync period
     * @return if all downloads were successful
     */
    private fun downloadAll(force: Boolean): Boolean {
        uploadMissingIds()
        val cafeSuccess = downloadCafeterias(force)
        val kinoSuccess = downloadKino(force)
        val newsSuccess = downloadNews(force)
        val eventsSuccess = downloadEvents()
        val topNewsSuccess = downloadTopNews()
        return cafeSuccess && kinoSuccess && newsSuccess && topNewsSuccess && eventsSuccess
    }

    /**
     * asks to verify private key, uploads fcm token and obfuscated ids (if missing)
     */
    private fun uploadMissingIds() {
        val lrzId = Utils.getSetting(this, Const.LRZ_ID, "")

        val uploadStatus = tumCabeClient.getUploadStatus(lrzId) ?: return
        Utils.log("upload missing ids: " + uploadStatus.toString())

        // upload FCM Token if not uploaded or invalid
        if (uploadStatus.fcmToken != UploadStatus.UPLOADED) {
            Utils.log("upload fcm token")
            AuthenticationManager(this).tryToUploadFcmToken()
        }

        if (lrzId.isEmpty()) {
            return // nothing else to be done
        }

        // ask server to verify our key
        if (uploadStatus.publicKey == UploadStatus.UPLOADED) { // uploaded but not verified
            Utils.log("ask server to verify key")
            val keyStatus = tumCabeClient.verifyKey()
            if (keyStatus?.status != UploadStatus.VERIFIED) {
                return // we can only upload obfuscated ids if we are verified
            }
        }

        // upload obfuscated ids
        AuthenticationManager(this).uploadObfuscatedIds(uploadStatus)
    }

    private fun downloadCafeterias(force: Boolean): Boolean {
        CafeteriaMenuManager(this).downloadMenus(force)
        cafeteriaRemoteRepository.fetchCafeterias(force)
        return true
    }

    private fun downloadKino(force: Boolean): Boolean {
        kinoRemoteRepository.fetchKinos(force)
        return true
    }

    private fun downloadNews(force: Boolean): Boolean {
        NewsController(this).downloadFromExternal(force)
        return true
    }

    private fun downloadEvents(): Boolean {
        EventsController(this).downloadFromService()
        return true
    }


    private fun downloadTopNews(): Boolean {
        topNewsRemoteRepository.fetchNewsAlert()
        return true
    }

    /**
     * Import default location and opening hours from assets
     */
    @Throws(IOException::class)
    private fun importLocationsDefaults() {
        val dao = database.locationDao()

        if (dao.isEmpty) {
            Utils.readCsv(assets.open(CSV_LOCATIONS))
                    .map(Location.Companion::fromCSVRow)
                    .forEach(dao::replaceInto)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
        Utils.log("DownloadService service has stopped")
    }

    companion object {

        /**
         * Download broadcast identifier
         */
        @JvmField val BROADCAST_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_DOWNLOAD"
        private const val LAST_UPDATE = "last_update"
        private const val CSV_LOCATIONS = "locations.csv"

        /**
         * Gets the time when BackgroundService was called last time
         *
         * @param context Context
         * @return time when BackgroundService was executed last time
         */
        @JvmStatic fun lastUpdate(context: Context): Long = Utils.getSettingLong(context, LAST_UPDATE, 0L)

        /**
         * Download the data for a specific intent
         * note, that only one concurrent download() is possible with a static synchronized method!
         */
        @Synchronized
        private fun download(intent: Intent, service: DownloadService) {
            val action = intent.getStringExtra(Const.ACTION_EXTRA) ?: return

            var success = true
            val force = intent.getBooleanExtra(Const.FORCE_DOWNLOAD, false)
            val launch = intent.getBooleanExtra(Const.APP_LAUNCHES, false)

            // Check if device has a internet connection
            val backgroundServicePermitted = Utils.isBackgroundServicePermitted(service)

            if (NetUtils.isConnected(service) && (launch || backgroundServicePermitted)) {
                Utils.logv("Handle action <$action>")

                when (action) {
                    Const.EVENTS -> success = service.downloadEvents()
                    Const.NEWS -> success = service.downloadNews(force)
                    Const.CAFETERIAS -> success = service.downloadCafeterias(force)
                    Const.KINO -> success = service.downloadKino(force)
                    Const.TOP_NEWS -> success = service.downloadTopNews()
                    else -> {
                        success = service.downloadAll(force)

                        if (AccessTokenManager.hasValidAccessToken(service)) {
                            val cacheManager = CacheManager(service)
                            cacheManager.fillCache()
                        }
                    }
                }
            }

            // Update the last run time saved in shared prefs
            if (action == Const.DOWNLOAD_ALL_FROM_EXTERNAL) {
                try {
                    service.importLocationsDefaults()
                } catch (e: IOException) {
                    Utils.log(e)
                    success = false
                }

                if (success) {
                    Utils.setSetting(service, LAST_UPDATE, System.currentTimeMillis())
                }

                success = true
            }

            // After done the job, create an broadcast intent and send it. The receivers will be
            // informed that the download service has finished.
            Utils.logv("DownloadService was " + (if (success) "" else "not ") + "successful")
            if (success) {
                service.broadcastDownloadSuccess()
            } else {
                service.broadcastDownloadError(R.string.exception_unknown)
            }
        }

        @JvmStatic fun enqueueWork(context: Context, work: Intent) {
            Utils.log("Download work enqueued")
            JobIntentService.enqueueWork(context, DownloadService::class.java, Const.DOWNLOAD_SERVICE_JOB_ID, work)
        }

    }

}