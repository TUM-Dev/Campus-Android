package de.tum.`in`.tumcampusapp.service

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.eduroam.EduroamController
import de.tum.`in`.tumcampusapp.component.ui.eduroam.SetupEduroamActivity
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import org.jetbrains.anko.wifiManager

/**
 * Listens for android's ScanResultsAvailable broadcast and checks if eduroam is nearby.
 * If yes and eduroam has not been setup by now it shows an according notification.
 */
class ScanResultsAvailableReceiver : BroadcastReceiver() {

    /**
     * This method either gets called by broadcast directly or gets repeatedly triggered by the
     * WifiScanHandler, which starts scans at time periods, as long as an eduroam or lrz network is
     * visible. onReceive then continues to store information like dBm and SSID to the local database.
     * The SyncManager then takes care of sending the Wifi measurements to the server in a given time
     * interval.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SCAN_RESULTS_AVAILABLE_ACTION) {
            return
        }

        if (!context.wifiManager.isWifiEnabled) {
            return
        }

        // Check if locations are enabled
        val locationsEnabled =
                checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        if (!locationsEnabled) {
            // Stop here as wifi.getScanResults will either return an empty list or throw an
            // exception (on android 6.0.0)
            return
        }

        // Test if user has eduroam configured already
        val isEduroamConfigured = EduroamController.getEduroamConfig(context) != null ||
                NetUtils.isConnected(context)

        context.wifiManager.scanResults.forEach { network ->
            if (network.SSID != Const.EDUROAM_SSID && network.SSID != Const.LRZ) {
                return@forEach
            }

            if (network.SSID == Const.EDUROAM_SSID && !isEduroamConfigured) {
                showNotification(context)
            }
        }

        // ???
        if (!Utils.getSettingBool(context, SHOULD_SHOW, true)) {
            Utils.setSetting(context, SHOULD_SHOW, true)
        }
    }

    class NeverShowAgainService : IntentService(NEVER_SHOW) {
        override fun onHandleIntent(intent: Intent) {
            Utils.setSetting(this, "card_eduroam_phone", false)
        }

        companion object {
            private const val NEVER_SHOW = "never_show"
        }
    }

    companion object {

        private const val SHOULD_SHOW = "wifi_setup_notification_dismissed"
        private const val NOTIFICATION_ID = 123

        /**
         * Shows notification if it is not already visible
         *
         * @param context Context
         */
        @JvmStatic
        fun showNotification(context: Context) {
            // If previous notification is still visible
            if (!Utils.getSettingBool(context, SHOULD_SHOW, true)) {
                return
            }

            // Prepare intents for notification actions
            val setupIntent = Intent(context, SetupEduroamActivity::class.java)
            val hideIntent = Intent(context, NeverShowAgainService::class.java)

            val setupPendingIntent = PendingIntent.getActivity(context, 0, setupIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val hidePendingIntent = PendingIntent.getService(context, 0, hideIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            // Create FcmNotification using NotificationCompat.Builder
            val notification = NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_EDUROAM)
                    .setSmallIcon(R.drawable.ic_notification_wifi)
                    .setTicker(context.getString(R.string.setup_eduroam))
                    .setContentTitle(context.getString(R.string.setup_eduroam))
                    .setContentText(context.getString(R.string.eduroam_setup_question))
                    .addAction(R.drawable.ic_action_cancel, context.getString(R.string.not_ask_again), hidePendingIntent)
                    .addAction(R.drawable.ic_notification_wifi, context.getString(R.string.setup), setupPendingIntent)
                    .setContentIntent(setupPendingIntent)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(context, R.color.color_primary))
                    .build()

            // Create FcmNotification Manager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)

            Utils.setSetting(context, SHOULD_SHOW, false)
        }
    }
}
