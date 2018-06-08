package de.tum.`in`.tumcampusapp.service

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import android.support.v4.content.LocalBroadcastManager
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaViewModel
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Location
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Location.Companion.fromCSVRow
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.news.KinoViewModel
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsViewModel
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.news.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.CacheManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.sync.SyncManager
import io.reactivex.disposables.CompositeDisposable
import java.io.IOException

/**
 * Service used to download files from external pages
 */
class DownloadService : JobIntentService() {

    private lateinit var broadcastManager: LocalBroadcastManager
    private lateinit var cafeteriaViewModel: CafeteriaViewModel

    private lateinit var kinoViewModel: KinoViewModel
    private lateinit var topNewsViewModel: TopNewsViewModel

    private val disposable = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        Utils.log("DownloadService service has started")
        broadcastManager = LocalBroadcastManager.getInstance(this)

        SyncManager(this) // Starts a new sync in constructor; should be moved to explicit method call

        val remoteRepository = CafeteriaRemoteRepository
        remoteRepository.tumCabeClient = TUMCabeClient.getInstance(this)

        val localRepository = CafeteriaLocalRepository
        localRepository.db = TcaDb.getInstance(this)
        cafeteriaViewModel = CafeteriaViewModel(localRepository, remoteRepository, disposable)

        // Init sync table
        KinoLocalRepository.db = TcaDb.getInstance(this)
        KinoRemoteRepository.tumCabeClient = TUMCabeClient.getInstance(this)
        kinoViewModel = KinoViewModel(KinoLocalRepository, KinoRemoteRepository, disposable)

        TopNewsRemoteRepository.tumCabeClient = TUMCabeClient.getInstance(this)
        topNewsViewModel = TopNewsViewModel(TopNewsRemoteRepository, disposable)
    }

    override fun onHandleWork(intent: Intent) {
        Thread {
            download(intent, this@DownloadService)
        }.start()
    }

    private fun broadcastDownloadSuccess() {
        sendServiceBroadcast(Const.COMPLETED, null)
    }

    private fun broadcastDownloadError(message: String) {
        sendServiceBroadcast(Const.ERROR, message)
    }

    private fun sendServiceBroadcast(action: String, message: String?) {
        val intent = Intent(BROADCAST_NAME).apply {
            putExtra(Const.ACTION_EXTRA, action)
            putExtra(Const.MESSAGE, message)
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
        val cafeSuccess = downloadCafeterias(force)
        val kinoSuccess = downloadKino(force)
        val newsSuccess = downloadNews(force)
        val topNewsSuccess = downloadTopNews()
        return cafeSuccess && kinoSuccess && newsSuccess && topNewsSuccess
    }

    private fun downloadCafeterias(force: Boolean): Boolean {
        CafeteriaMenuManager(this)
                .downloadFromExternal(this, force)
        cafeteriaViewModel.getCafeteriasFromService(force)
        return true
    }

    private fun downloadKino(force: Boolean): Boolean {
        kinoViewModel.getKinosFromService(force)
        return true
    }

    private fun downloadNews(force: Boolean): Boolean {
        NewsController(this).downloadFromExternal(force)
        return true
    }

    private fun downloadTopNews() = topNewsViewModel.getNewsAlertFromService(this)

    /**
     * Import default location and opening hours from assets
     */
    @Throws(IOException::class)
    private fun importLocationsDefaults() {
        val dao = TcaDb
                .getInstance(this)
                .locationDao()

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
                    Const.NEWS -> success = service.downloadNews(force)
                    Const.CAFETERIAS -> success = service.downloadCafeterias(force)
                    Const.KINO -> success = service.downloadKino(force)
                    Const.TOP_NEWS -> success = service.downloadTopNews()
                    else -> {
                        success = service.downloadAll(force)
                        val isSetup = Utils.getSettingBool(service, Const.EVERYTHING_SETUP, false)
                        if (!isSetup) {
                            CacheManager(service).syncCalendar()
                            if (success) {
                                Utils.setSetting(service, Const.EVERYTHING_SETUP, true)
                            }
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

                // TODO Till: Philipp and I have decided to omit the update of Cards here.
                // In the future, we’ll introduce a better way for Manager classes to update
                // themselves in the background, independent of their corresponding Card.

                success = true
            }

            // After done the job, create an broadcast intent and send it. The receivers will be informed that the download service has finished.
            Utils.logv("DownloadService was " + (if (success) "" else "not ") + "successful")
            if (success) {
                service.broadcastDownloadSuccess()
            } else {
                service.broadcastDownloadError(service.getString(R.string.exception_unknown))
            }

            // Do all other import stuff that is not relevant for creating the viewing the start page
            if (action == Const.DOWNLOAD_ALL_FROM_EXTERNAL) {
                FillCacheService.enqueueWork(service.baseContext, Intent())
            }
        }

        @JvmStatic fun enqueueWork(context: Context, work: Intent) {
            Utils.log("Download work enqueued")
            JobIntentService.enqueueWork(context, DownloadService::class.java, Const.DOWNLOAD_SERVICE_JOB_ID, work)
        }

    }

}
