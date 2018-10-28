package de.tum.`in`.tumcampusapp.service

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result.SUCCESS
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.tum.`in`.tumcampusapp.utils.Const.APP_LAUNCHES
import de.tum.`in`.tumcampusapp.utils.Const.ACTION_EXTRA
import de.tum.`in`.tumcampusapp.utils.Const.DOWNLOAD_ALL_FROM_EXTERNAL
import de.tum.`in`.tumcampusapp.utils.Const.FORCE_DOWNLOAD

/**
 * Worker to sync data periodically in background
 */
class BackgroundWorker(context: Context, workerParams: WorkerParameters) :
        Worker(context, workerParams) {

    /**
     * Starts [DownloadWorker] with appropriate extras
     */
    override fun doWork(): ListenableWorker.Result {
        // Download all from external
        val appLaunches = inputData.getBoolean(APP_LAUNCHES, false)
        val service = Intent().apply {
            putExtra(ACTION_EXTRA, DOWNLOAD_ALL_FROM_EXTERNAL)
            putExtra(FORCE_DOWNLOAD, false)
            putExtra(APP_LAUNCHES, appLaunches)
        }
        // TODO
        // DownloadService.enqueueWork(baseContext, service)

        if (Looper.myLooper() == null) {
            Looper.prepare()
        }

        WifiScanHandler.getInstance().startRepetition(applicationContext)

        return SUCCESS
    }
}