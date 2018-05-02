package de.tum.`in`.tumcampusapp.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

        // Look up background service settingsPrefix
        val backgroundServicePermitted = Utils.isBackgroundServicePermitted(context)

        // Set AlarmNotification for next update, if background service is enabled
        if (backgroundServicePermitted) {
            setAlarm(context)
        }

        // Start BackgroundService
        if (isLaunch || backgroundServicePermitted) {
            Utils.logv("Start background service...")
            val newIntent = Intent().apply {
                putExtra(Const.APP_LAUNCHES, isLaunch)
            }
            BackgroundService.enqueueWork(context, newIntent)
        }

        SendMessageService.enqueueWork(context, Intent())

        // Also start the SilenceService. It checks if it is enabled, so we don't need to
        SilenceService.enqueueWork(context, Intent())
        if (intent.action != ACTION_WIFI_STATE_CHANGED && Utils.getSettingBool(context, Const.WIFI_SCANS_ALLOWED, false)) {
            SendWifiMeasurementService.enqueueWork(context, Intent())
        }
    }

    companion object {

        private const val ACTION_WIFI_STATE_CHANGED = "android.net.wifi.WIFI_STATE_CHANGED"
        private const val START_INTERVAL = AlarmManager.INTERVAL_HOUR * 3

        private fun setAlarm(context: Context) {
            // Intent to call on alarm
            val intent = Intent(context, StartSyncReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

            // Set alarm
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.cancel(pendingIntent)
            alarm.setExact(AlarmManager.RTC, System.currentTimeMillis() + StartSyncReceiver.START_INTERVAL, pendingIntent)
        }
    }
}