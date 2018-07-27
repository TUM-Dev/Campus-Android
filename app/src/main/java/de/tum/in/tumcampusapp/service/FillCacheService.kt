package de.tum.`in`.tumcampusapp.service

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import de.tum.`in`.tumcampusapp.utils.CacheManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Service used to fill caches in background, for faster/offline access
 */
class FillCacheService : JobIntentService() {

    override fun onCreate() {
        super.onCreate()
        Utils.logv("FillCacheService has started")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.logv("FillCacheService has stopped")
    }

    override fun onHandleWork(intent: Intent) {
        val cacheManager = CacheManager(this@FillCacheService)
        cacheManager.fillCache()
    }

    companion object {

        @JvmStatic fun enqueueWork(context: Context, work: Intent) {
            JobIntentService.enqueueWork(context,
                    FillCacheService::class.java, Const.FILL_CACHE_SERVICE_JOB_ID, work)
        }

    }
}