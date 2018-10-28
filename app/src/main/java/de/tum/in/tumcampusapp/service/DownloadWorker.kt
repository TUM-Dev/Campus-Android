package de.tum.`in`.tumcampusapp.service

import android.content.Context
import androidx.work.Data
import androidx.work.ListenableWorker.Result.RETRY
import androidx.work.ListenableWorker.Result.SUCCESS
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.UploadStatus
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaViewModel
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Location
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.news.KinoViewModel
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsViewModel
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsController
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.CacheManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.sync.SyncManager
import io.reactivex.disposables.CompositeDisposable
import java.io.IOException


class DownloadWorker(context: Context, workerParams: WorkerParameters) :
        Worker(context, workerParams) {

    private val tumCabeClient by lazy { TUMCabeClient.getInstance(applicationContext) }
    private val database by lazy { TcaDb.getInstance(applicationContext) }

    private val cafeteriaViewModel: CafeteriaViewModel
    private val kinoViewModel: KinoViewModel
    private val topNewsViewModel: TopNewsViewModel

    private val disposable = CompositeDisposable()

    init {
        Utils.log("DownloadService service has started")

        // Starts a new sync in constructor; should be moved to explicit method call
        SyncManager(applicationContext)

        CafeteriaRemoteRepository.tumCabeClient = tumCabeClient
        CafeteriaLocalRepository.db = database
        cafeteriaViewModel = CafeteriaViewModel(CafeteriaLocalRepository, CafeteriaRemoteRepository, disposable)

        KinoLocalRepository.db = database
        KinoRemoteRepository.tumCabeClient = tumCabeClient
        kinoViewModel = KinoViewModel(KinoLocalRepository, KinoRemoteRepository, disposable)

        TopNewsRemoteRepository.tumCabeClient = tumCabeClient
        topNewsViewModel = TopNewsViewModel(TopNewsRemoteRepository, disposable)
    }

    override fun doWork(): Result {
        return try {
            download(inputData, this)
            Utils.setSetting(applicationContext, LAST_UPDATE, System.currentTimeMillis())
            SUCCESS
        } catch (e: Exception) {
            Utils.log(e)
            RETRY
        }
    }

    /**
     * Download all external data and returns whether the download was successful
     *
     * @param force True to force download over normal sync period
     * @return if all downloads were successful
     */
    private fun downloadAll(force: Boolean) {
        uploadMissingIds()
        downloadCafeterias(force)
        downloadKino(force)
        downloadNews(force)
        downloadEvents()
        downloadTopNews()
    }

    /**
     * asks to verify private key, uploads fcm token and obfuscated ids (if missing)
     */
    private fun uploadMissingIds() {
        val lrzId = Utils.getSetting(applicationContext, Const.LRZ_ID, "")

        val uploadStatus = tumCabeClient.getUploadStatus(lrzId) ?: return
        Utils.log("upload missing ids: " + uploadStatus.toString())

        // upload FCM Token if not uploaded or invalid
        if (uploadStatus.fcmToken != UploadStatus.UPLOADED) {
            Utils.log("upload fcm token")
            AuthenticationManager(applicationContext).tryToUploadFcmToken()
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
        AuthenticationManager(applicationContext).uploadObfuscatedIds(uploadStatus)
    }

    private fun downloadCafeterias(force: Boolean) {
        CafeteriaMenuManager(applicationContext).downloadMenus(force)
        cafeteriaViewModel.getCafeteriasFromService(force)
    }

    private fun downloadKino(force: Boolean) = kinoViewModel.getKinosFromService(force)

    private fun downloadNews(force: Boolean) =
            NewsController(applicationContext).downloadFromExternal(force)

    private fun downloadEvents() = EventsController(applicationContext).downloadFromService()


    private fun downloadTopNews() = topNewsViewModel.getNewsAlertFromService(applicationContext)

    /**
     * Import default location and opening hours from assets
     */
    @Throws(IOException::class)
    private fun importLocationsDefaults() {
        val dao = database.locationDao()
        if (dao.isEmpty) {
            Utils.readCsv(applicationContext.assets.open(CSV_LOCATIONS))
                    .map(Location.Companion::fromCSVRow)
                    .forEach(dao::replaceInto)
        }
    }

    companion object {
        private const val LAST_UPDATE = "last_update"
        private const val CSV_LOCATIONS = "locations.csv"

        /**
         * Gets the time when BackgroundService was called last time
         *
         * @param context Context
         * @return time when BackgroundService was executed last time
         */
        @JvmStatic
        fun lastUpdate(context: Context): Long = Utils.getSettingLong(context, LAST_UPDATE, 0L)

        /**
         * Download the data for a specific intent
         * note, that only one concurrent download() is possible with a static synchronized method!
         */
        @Synchronized
        private fun download(data: Data, service: DownloadWorker) {
            val action = data.getString(Const.ACTION_EXTRA) ?: return
            val force = data.getBoolean(Const.FORCE_DOWNLOAD, false)
            val launch = data.getBoolean(Const.APP_LAUNCHES, false)


            // Check if device has a internet connection
            val backgroundServicePermitted = Utils.isBackgroundServicePermitted(service.applicationContext)

            if (NetUtils.isConnected(service.applicationContext) && (launch || backgroundServicePermitted)) {
                Utils.logv("Handle action <$action>")

                when (action) {
                    Const.EVENTS -> service.downloadEvents()
                    Const.NEWS -> service.downloadNews(force)
                    Const.CAFETERIAS -> service.downloadCafeterias(force)
                    Const.KINO -> service.downloadKino(force)
                    Const.TOP_NEWS -> service.downloadTopNews()
                    else -> {
                        service.downloadAll(force)

                        if (AccessTokenManager.hasValidAccessToken(service.applicationContext)) {
                            val cacheManager = CacheManager(service.applicationContext)
                            cacheManager.fillCache()
                        }
                    }
                }
            }

            // Update the last run time saved in shared prefs
            if (action == Const.DOWNLOAD_ALL_FROM_EXTERNAL) {
                service.importLocationsDefaults()
            }
        }
    }
}