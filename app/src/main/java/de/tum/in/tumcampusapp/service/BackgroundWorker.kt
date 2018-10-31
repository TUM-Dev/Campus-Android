package de.tum.`in`.tumcampusapp.service

import android.content.Context
import android.os.Looper
import androidx.work.*
import androidx.work.ListenableWorker.Result.SUCCESS
import de.tum.`in`.tumcampusapp.utils.Const.APP_LAUNCHES
import de.tum.`in`.tumcampusapp.utils.Const.DOWNLOAD_ALL_FROM_EXTERNAL
import java.util.concurrent.TimeUnit

/**
 * Worker to sync data periodically in background
 */
class BackgroundWorker(context: Context, workerParams: WorkerParameters) :
        Worker(context, workerParams) {

    /**
     * Starts [DownloadWorker] with appropriate extras
     */
    override fun doWork(): ListenableWorker.Result {
        val appLaunches = inputData.getBoolean(APP_LAUNCHES, false)

        // Trigger periodic download in background
        WorkManager.getInstance()
                .beginUniqueWork(UNIQUE_DOWNLOAD, ExistingWorkPolicy.KEEP,
                        DownloadWorker.getWorkRequest(
                                DOWNLOAD_ALL_FROM_EXTERNAL, false, appLaunches)
                ).enqueue()

        if (Looper.myLooper() == null) {
            Looper.prepare()
        }

        WifiScanHandler.getInstance().startRepetition(applicationContext)
        return SUCCESS
    }

    companion object {
        private const val UNIQUE_DOWNLOAD = "BACKGROUND_DOWNLOAD"

        fun getWorkRequest(appLaunches: Boolean = false): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            val data = Data.Builder()
                    .putBoolean(APP_LAUNCHES, appLaunches)
                    .build()
            return PeriodicWorkRequestBuilder<BackgroundWorker>(3, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .setInputData(data)
                    .build()
        }
    }
}