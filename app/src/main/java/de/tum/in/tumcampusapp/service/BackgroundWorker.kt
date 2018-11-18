package de.tum.`in`.tumcampusapp.service

import android.content.Context
import androidx.work.*
import androidx.work.ListenableWorker.Result.SUCCESS
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
        // Trigger periodic download in background
        WorkManager.getInstance()
                .beginUniqueWork(UNIQUE_DOWNLOAD, ExistingWorkPolicy.KEEP,
                        DownloadWorker.getWorkRequest()
                ).enqueue()

        return SUCCESS
    }

    companion object {
        private const val UNIQUE_DOWNLOAD = "BACKGROUND_DOWNLOAD"

        fun getWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            return PeriodicWorkRequestBuilder<BackgroundWorker>(3, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build()
        }
    }
}