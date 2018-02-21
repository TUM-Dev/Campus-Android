package de.tum.in.tumcampusapp.service;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.util.Calendar;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.wifimeasurement.WifiMeasurementLocationListener;
import de.tum.in.tumcampusapp.component.other.wifimeasurement.model.WifiMeasurement;
import de.tum.in.tumcampusapp.component.ui.eduroam.EduroamController;
import de.tum.in.tumcampusapp.component.ui.eduroam.SetupEduroamActivity;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Listens for android's ScanResultsAvailable broadcast and checks if eduroam is nearby.
 * If yes and eduroam has not been setup by now it shows an according notification.
 */
public class ScanResultsAvailableReceiver extends BroadcastReceiver {
    private static final String SHOULD_SHOW = "wifi_setup_notification_dismissed";
    private static LocationManager locationManager;

    @Override
    /**
     * This method either gets called by broadcast directly or gets repeatedly triggered by the
     * WifiScanHandler, which starts scans at time periods, as long as an eduroam or lrz network is
     * visible. onReceive then continues to store information like dBm and SSID to the local database.
     * The SyncManager then takes care of sending the Wifi measurements to the server in a given time
     * interval.
     */
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction()
                   .equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            return;
        }
        //Check if wifi is turned on at all
        WifiManager wifi = (WifiManager) context.getApplicationContext()
                                                .getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            return;
        }

        if (locationManager == null) {
            locationManager = (LocationManager) context.getApplicationContext()
                                                       .getSystemService(Context.LOCATION_SERVICE);
        }

        // Test if user has eduroam configured already
        boolean eduroamConfiguredAlready = EduroamController.getEduroamConfig(context) != null || NetUtils.isConnected(context) || Build.VERSION.SDK_INT < 18;

        //Check if locations are enabled
        boolean locationsEnabled = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean wifiScansEnabled = Utils.getInternalSettingBool(context, Const.WIFI_SCANS_ALLOWED, false);
        boolean nextScanScheduled = false;

        if (!locationsEnabled) {
            //Stop here as wifi.getScanResults will either return an empty list or throw an exception (on android 6.0.0)
            return;
        }

        WifiScanHandler wifiScanHandler = WifiScanHandler.getInstance(context);
        List<ScanResult> scan = wifi.getScanResults();
        for (final ScanResult network : scan) {
            //skips if network is not either eduroam or lrz network
            if (!(network.SSID.equals("eduroam") || network.SSID.equals("lrz"))) {
                continue;
            }
            //if eduroam is not configured, set it up
            if (network.SSID.equals("eduroam") && !eduroamConfiguredAlready) {
                showNotification(context);
            }

            //if user allowed us to store his signal strength, store measurement to the local DB and later sync to remote
            if (wifiScansEnabled) {
                storeWifiMeasurement(context, network);
                nextScanScheduled = true;
            }
        }

        if (nextScanScheduled) {
            //WIFI_SCAN_MINIMUM_BATTERY_LEVEL is used to decide, whether another Wifi-Scan is initiated on
            //encountering an eduroam/lrz network. If the battery is lower, no new automatic scan will be
            //scheduled. This setting can be used as additional way to limit battery consumption and leaves
            //the user more freedom in deciding, when to scan.
            float currentBattery = Utils.getBatteryLevel(context);
            float minimumBattery = Utils.getInternalSettingFloat(context, Const.INSTANCE.WIFI_SCAN_MINIMUM_BATTERY_LEVEL, 50.0f);
            if (currentBattery > minimumBattery) {
                wifiScanHandler.startRepetition();
                Utils.log("WifiScanHandler rescheduled");
            } else {
                Utils.log("WifiScanHandler stopped");
            }
        }

        //???
        if (!Utils.getInternalSettingBool(context, SHOULD_SHOW, true)) {
            Utils.setInternalSetting(context, SHOULD_SHOW, true);
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

    private void storeWifiMeasurement(Context context, ScanResult scanResult) throws SecurityException {
        Criteria criteria = new Criteria();
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        WifiMeasurement wifiMeasurement = new WifiMeasurement("", scanResult.SSID, scanResult.BSSID, scanResult.level, -1, -1, -1);
        locationManager.requestSingleUpdate(criteria, new WifiMeasurementLocationListener(context, wifiMeasurement, Calendar.getInstance()
                                                                                                                            .getTimeInMillis()), null);
    }

    /**
     * Shows notification if it is not already visible
     *
     * @param context Context
     */
    static void showNotification(Context context) {
        // If previous notification is still visible
        if (!Utils.getInternalSettingBool(context, SHOULD_SHOW, true)) {
            return;
        }

        // Prepare intents for notification actions
        Intent intent = new Intent(context, SetupEduroamActivity.class);
        Intent hide = new Intent(context, NeverShowAgain.class);

        PendingIntent setupIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent hideIntent = PendingIntent.getService(context, 0, hide, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create GCMNotification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_EDUROAM)
                .setSmallIcon(R.drawable.ic_notification_wifi)
                .setTicker(context.getString(R.string.setup_eduroam))
                .setContentTitle(context.getString(R.string.setup_eduroam))
                .setContentText(context.getString(R.string.eduroam_setup_question))
                .addAction(R.drawable.ic_action_cancel, context.getString(R.string.not_ask_again), hideIntent)
                .addAction(R.drawable.ic_notification_wifi, context.getString(R.string.setup), setupIntent)
                .setContentIntent(setupIntent)
                .setAutoCancel(true);

        // Create GCMNotification Manager
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Build GCMNotification with GCMNotification Manager
        notificationmanager.notify(123, builder.build());

        Utils.setInternalSetting(context, SHOULD_SHOW, false);

    }

    public static class NeverShowAgain extends IntentService {

        private static final String NEVER_SHOW = "never_show";

        public NeverShowAgain() {
            super(NEVER_SHOW);
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Utils.setSetting(this, "card_eduroam_phone", false);
        }
    }
}
