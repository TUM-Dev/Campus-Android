package de.tum.`in`.tumcampusapp.service

import android.Manifest
import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.wifimeasurement.WifiMeasurementLocationListener
import de.tum.`in`.tumcampusapp.component.other.wifimeasurement.model.WifiMeasurement
import de.tum.`in`.tumcampusapp.component.ui.eduroam.EduroamController
import de.tum.`in`.tumcampusapp.component.ui.eduroam.SetupEduroamActivity
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Listens for android's ScanResultsAvailable broadcast and checks if eduroam is nearby.
 * If yes and eduroam has not been setup by now it shows an according notification.
 */
class ScanResultsAvailableReceiver : BroadcastReceiver() {

    private var locationManager: LocationManager? = null

    /**
     * This method either gets called by broadcast directly or gets repeatedly triggered by the
     * WifiScanHandler, which starts scans at time periods, as long as an eduroam or lrz network is
     * visible. onReceive then continues to store information like dBm and SSID to the local database.
     * The SyncManager then takes care of sending the Wifi measurements to the server in a given time
     * interval.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
            return
        }

        WifiScanHandler.getInstance().onScanFinished()

        //Check if wifi is turned on at all
        val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            return
        }

        locationManager = context.applicationContext
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //Check if locations are enabled
        val locationsEnabled = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!locationsEnabled) {
            //Stop here as wifi.getScanResults will either return an empty list or throw an exception (on android 6.0.0)
            return
        }

        // Test if user has eduroam configured already
        val isEduroamConfigured = EduroamController.getEduroamConfig(context) != null || NetUtils.isConnected(context)

        val wifiScansEnabled = Utils.getSettingBool(context, Const.WIFI_SCANS_ALLOWED, false)
        var nextScanScheduled = false

        wifiManager.scanResults.forEach { network ->
            if (network.SSID != Const.EDUROAM_SSID && network.SSID != Const.LRZ) {
                return@forEach
            }

            if (network.SSID == Const.EDUROAM_SSID && !isEduroamConfigured) {
                showNotification(context)
            }

            if (wifiScansEnabled) {
                storeWifiMeasurement(context, network)
                nextScanScheduled = true
            }
        }

        if (nextScanScheduled) {
            //WIFI_SCAN_MINIMUM_BATTERY_LEVEL is used to decide, whether another Wifi-Scan is initiated on
            //encountering an eduroam/lrz network. If the battery is lower, no new automatic scan will be
            //scheduled. This setting can be used as additional way to limit battery consumption and leaves
            //the user more freedom in deciding, when to scan.
            val currentBattery = Utils.getBatteryLevel(context)
            val minimumBattery = Utils.getSettingFloat(context, Const.WIFI_SCAN_MINIMUM_BATTERY_LEVEL, 50.0f)

            if (currentBattery > minimumBattery) {
                Utils.log("WifiScanHandler rescheduled")
            } else {
                Utils.log("WifiScanHandler stopped")
            }
        }

        //???
        if (!Utils.getSettingBool(context, SHOULD_SHOW, true)) {
            Utils.setSetting(context, SHOULD_SHOW, true)
        }
    }

    /**
     * This method stores wifi scan results to the server. When they first get created by the
     * ScanResultsAvailable's onReceive method, they lack gps information for creating a heatmap.
     * Therefore we request an update from the WifiMeasurementLocationListener, passing the incomplete WifiMeasurement.
     * The WifiMeasurementLocationListener then takes care of adding the location information, whenever it is ready.
     *
     * @param context
     * @param scanResult
     */

    @Throws(SecurityException::class)
    private fun storeWifiMeasurement(context: Context, scanResult: ScanResult) {
        val criteria = Criteria().apply {
            horizontalAccuracy = Criteria.ACCURACY_FINE
            isBearingRequired = false
            isAltitudeRequired = false
            isSpeedRequired = false
        }

        val wifiMeasurement = WifiMeasurement.fromScanResult(scanResult)
        val listener = WifiMeasurementLocationListener(context, wifiMeasurement, System.currentTimeMillis())
        locationManager?.requestSingleUpdate(criteria, listener, null)
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
        @JvmStatic fun showNotification(context: Context) {
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
                    .build()

            // Create FcmNotification Manager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)

            Utils.setSetting(context, SHOULD_SHOW, false)
        }

    }

}
