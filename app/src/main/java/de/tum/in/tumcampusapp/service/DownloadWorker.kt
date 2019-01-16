package de.tum.`in`.tumcampusapp.service

import android.content.Context
import androidx.work.*
import androidx.work.ListenableWorker.Result.RETRY
import androidx.work.ListenableWorker.Result.SUCCESS
import androidx.work.NetworkType.CONNECTED
import de.tum.`in`.tumcampusapp.api.app.IdUploadAction
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl.BYPASS_CACHE
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl.USE_CACHE
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaLocationImportAction
import de.tum.`in`.tumcampusapp.component.ui.news.NewsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.tufilm.FilmDownloadAction
import de.tum.`in`.tumcampusapp.di.injector
import de.tum.`in`.tumcampusapp.service.di.DownloadModule
import de.tum.`in`.tumcampusapp.utils.CacheManager
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class DownloadWorker(
        context: Context,
        workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @Inject
    lateinit var cafeteriaDownloadAction: CafeteriaDownloadAction

    @Inject
    lateinit var cafeteriaLocationImportAction: CafeteriaLocationImportAction

    @Inject
    lateinit var eventsDownloadAction: EventsDownloadAction

    @Inject
    lateinit var filmDownloadAction: FilmDownloadAction

    @Inject
    lateinit var idUploadAction: IdUploadAction

    @Inject
    lateinit var newsDownloadAction: NewsDownloadAction

    @Inject
    lateinit var topNewsDownloadAction: TopNewsDownloadAction

    private val downloadActions = listOf(
            cafeteriaDownloadAction,
            cafeteriaLocationImportAction,
            eventsDownloadAction,
            filmDownloadAction,
            idUploadAction,
            newsDownloadAction,
            topNewsDownloadAction
    )

    init {
        Utils.log("DownloadService service has started")
    }

    override fun doWork(): Result {
        injector.downloadComponent()
                .downloadModule(DownloadModule())
                .build()
                .inject(this)

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
     * @param behaviour BYPASS_CACHE to force download over normal sync period
     * @return if all downloads were successful
     */
    private fun downloadAll(behaviour: CacheControl) {
        downloadActions.forEach { it.execute(behaviour) }
    }

    interface Action {
        fun execute(cacheBehaviour: CacheControl)
    }

    companion object {

        private const val FORCE_DOWNLOAD = "FORCE_DOWNLOAD"
        private const val FILL_CACHE = "FILL_CACHE"
        private const val LAST_UPDATE = "LAST_UPDATE"

        @JvmOverloads
        @JvmStatic
        fun getWorkRequest(
                behaviour: CacheControl = USE_CACHE,
                fillCache: Boolean = false
        ): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(CONNECTED)
                    .build()
            val data = Data.Builder()
                    .putBoolean(FORCE_DOWNLOAD, behaviour == BYPASS_CACHE)
                    .putBoolean(FILL_CACHE, fillCache)
                    .build()
            return OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setConstraints(constraints)
                    .setInputData(data)
                    .build()
        }

        /**
         * Gets the time when BackgroundService was called last time
         *
         * @param context Context
         * @return time when BackgroundService was executed last time
         */
        @JvmStatic
        fun lastUpdate(context: Context) = Utils.getSettingLong(context, LAST_UPDATE, 0L)

        /**
         * Download the data for a specific intent
         * note, that only one concurrent download() is possible with a static synchronized method!
         */
        @Synchronized
        private fun download(data: Data, service: DownloadWorker) {
            val force = if (data.getBoolean(FORCE_DOWNLOAD, false)) BYPASS_CACHE else USE_CACHE

            val backgroundServicePermitted = Utils.isBackgroundServicePermitted(service.applicationContext)
            if (!NetUtils.isConnected(service.applicationContext) || !backgroundServicePermitted) {
                return
            }

            service.downloadAll(force)

            val hasValidToken = AccessTokenManager.hasValidAccessToken(service.applicationContext)
            val shouldFillCache = data.getBoolean(FILL_CACHE, false)

            if (hasValidToken && shouldFillCache) {
                val cacheManager = CacheManager(service.applicationContext)
                cacheManager.fillCache()
            }
        }

        @JvmStatic
        fun getAllDownloadActions(
                context: Context,
                disposable: CompositeDisposable
        ): List<(CacheControl) -> Unit> {
            return emptyList()
        }

    }

}
