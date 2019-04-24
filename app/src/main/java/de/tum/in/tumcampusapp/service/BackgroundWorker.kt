package de.tum.`in`.tumcampusapp.service

import android.content.Context
import androidx.work.*
import androidx.work.ListenableWorker.Result.success
import java.util.concurrent.TimeUnit

/**
 * Worker to sync data periodically in background
 */
class BackgroundWorker(
        context: Context,
        workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): ListenableWorker.Result {
        val downloadWorkRequest = DownloadWorker.getWorkRequest()
        WorkManager.getInstance()
                .beginUniqueWork(UNIQUE_DOWNLOAD, ExistingWorkPolicy.KEEP, downloadWorkRequest)
                .enqueue()

        return success()
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