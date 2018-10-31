package de.tum.`in`.tumcampusapp.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.WorkManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Receives on boot completed broadcast, sets alarm for next sync-try
 * and start BackgroundService if enabled in settingsPrefix
 */
class StartSyncReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        // Check intent if called from StartupActivity
        val isLaunch = intent.getBooleanExtra(Const.APP_LAUNCHES, false)
        startBackground(context, isLaunch)

        WorkManager.getInstance()
                .enqueue(SendMessageWorker.getWorkRequest())

        // Also start the SilenceService. It checks if it is enabled, so we don't need to
        // SilenceService also needs accurate timings, so we can't use WorkManager
        SilenceService.enqueueWork(context, Intent())
        if (intent.action != ACTION_WIFI_STATE_CHANGED && Utils.getSettingBool(context, Const.WIFI_SCANS_ALLOWED, false)) {
            SendWifiMeasurementService.enqueueWork(context, Intent())
        }
    }

    companion object {
        private const val ACTION_WIFI_STATE_CHANGED = "android.net.wifi.WIFI_STATE_CHANGED"
        private const val UNIQUE_BACKGROUND = "START_SYNC_BACKGROUND"

        /**
         * Start the periodic BackgroundWorker, ensuring only one task is ever running
         */
        @JvmStatic
        @JvmOverloads
        fun startBackground(context: Context, isLaunch: Boolean = false) {
            if (!Utils.isBackgroundServicePermitted(context)) {
                return
            }
            WorkManager.getInstance()
                    .enqueueUniquePeriodicWork(UNIQUE_BACKGROUND, KEEP,
                            BackgroundWorker.getWorkRequest(isLaunch))
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
