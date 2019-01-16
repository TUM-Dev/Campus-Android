package de.tum.`in`.tumcampusapp.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.WorkManager
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Receives on boot completed broadcast, sets alarm for next sync-try
 * and start BackgroundService if enabled in settingsPrefix
 */
class StartSyncReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        // Check intent if called from StartupActivity
        startBackground(context)

        startSendMessage()

        // Also start the SilenceService. It checks if it is enabled, so we don't need to
        // SilenceService also needs accurate timings, so we can't use WorkManager
        SilenceService.enqueueWork(context, Intent())
    }

    companion object {
        private const val UNIQUE_BACKGROUND = "START_SYNC_BACKGROUND"
        private const val UNIQUE_SEND_MESSAGE = "START_SYNC_SEND_MESSAGE"

        fun startSendMessage() {
            WorkManager.getInstance()
                    .enqueueUniquePeriodicWork(
                            UNIQUE_SEND_MESSAGE, KEEP, SendMessageWorker.getPeriodicWorkRequest())
        }

        /**
         * Start the periodic BackgroundWorker, ensuring only one task is ever running
         */
        @JvmStatic
        fun startBackground(context: Context) {
            if (!Utils.isBackgroundServicePermitted(context)) {
                return
            }
            WorkManager.getInstance()
                    .enqueueUniquePeriodicWork(UNIQUE_BACKGROUND, KEEP,
                            BackgroundWorker.getWorkRequest())
        }

        /**
         * Cancels the periodic BackgroundWorker
         */
        @JvmStatic
        fun cancelBackground() {
            WorkManager.getInstance()
                    .cancelUniqueWork(UNIQUE_BACKGROUND)
        }
    }
}
