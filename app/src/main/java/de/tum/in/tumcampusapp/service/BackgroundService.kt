package de.tum.`in`.tumcampusapp.service

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Service used to sync data in background
 */
class BackgroundService : JobIntentService() {

    override fun onCreate() {
        super.onCreate()
        Utils.log("BackgroundService has started")
    }

    /**
     * Starts [DownloadService] with appropriate extras
     *
     * @param intent Intent
     */
    override fun onHandleWork(intent: Intent) {
        // Download all from external
        val service = Intent().apply {
            putExtra(Const.ACTION_EXTRA, Const.DOWNLOAD_ALL_FROM_EXTERNAL)
            putExtra(Const.FORCE_DOWNLOAD, false)
            putExtra(Const.APP_LAUNCHES, intent.getBooleanExtra(Const.APP_LAUNCHES, false))
        }
        service.putExtra(Const.ACTION_EXTRA, Const.DOWNLOAD_ALL_FROM_EXTERNAL)
        service.putExtra(Const.FORCE_DOWNLOAD, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.log("BackgroundService has stopped")
    }

    companion object {

        @JvmStatic fun enqueueWork(context: Context, work: Intent) {
            JobIntentService.enqueueWork(context, BackgroundService::class.java, Const.BACKGROUND_SERVICE_JOB_ID, work)
        }

    }
}