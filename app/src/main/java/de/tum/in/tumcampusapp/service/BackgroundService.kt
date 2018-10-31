package de.tum.`in`.tumcampusapp.service

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.core.app.JobIntentService
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
        val appLaunches = intent.getBooleanExtra(Const.APP_LAUNCHES, false)
        val service = Intent().apply {
            putExtra(Const.ACTION_EXTRA, Const.DOWNLOAD_ALL_FROM_EXTERNAL)
            putExtra(Const.FORCE_DOWNLOAD, false)
            putExtra(Const.APP_LAUNCHES, appLaunches)
        }
        DownloadService.enqueueWork(baseContext, service)

        if (Looper.myLooper() == null) {
            Looper.prepare()
        }

        WifiScanHandler.getInstance().startRepetition(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.log("BackgroundService has stopped")
    }

    companion object {

        @JvmStatic fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, BackgroundService::class.java, Const.BACKGROUND_SERVICE_JOB_ID, work)
        }

    }

}