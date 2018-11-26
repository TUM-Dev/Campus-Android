package de.tum.`in`.tumcampusapp.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import de.tum.`in`.tumcampusapp.utils.BackgroundUpdater
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.injector
import javax.inject.Inject

/**
 * Service used to fill caches in background, for faster/offline access
 */
class FillCacheService : JobIntentService() {

    @Inject
    lateinit var backgroundUpdater: BackgroundUpdater

    override fun onCreate() {
        super.onCreate()
        injector.inject(this)
        Utils.logv("FillCacheService has started")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.logv("FillCacheService has stopped")
    }

    override fun onHandleWork(intent: Intent) {
        backgroundUpdater.update()
    }

    companion object {

        @JvmStatic fun enqueueWork(context: Context, work: Intent) {
            JobIntentService.enqueueWork(context,
                    FillCacheService::class.java, Const.FILL_CACHE_SERVICE_JOB_ID, work)
        }

    }
}