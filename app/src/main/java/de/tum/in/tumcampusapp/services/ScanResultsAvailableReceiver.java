package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.SetupEduroamActivity;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.EduroamManager;

/**
 * Listens for android's ScanResultsAvailable broadcast and checks if eduroam is nearby.
 * If yes and eduroam has not been setup by now it shows an according notification.
 */
public class ScanResultsAvailableReceiver extends BroadcastReceiver {
    private static final String SHOULD_SHOW = "wifi_setup_notification_dismissed";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            return;
        }

        // Test if user has eduroam configured already
        if (EduroamManager.getEduroamConfig(context) != null || NetUtils.isConnected(context) || Build.VERSION.SDK_INT < 18) {
            return;
        }

        //Check if wifi is turned on at all, as we cannot say if it was configured if its off
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifi.isWifiEnabled()) {
            return;
        }

        // Test if eduroam is available
        List<ScanResult> scan = wifi.getScanResults();
        for (ScanResult network : scan) {
            if (network.SSID.equals(EduroamManager.NETWORK_SSID)) {
                //Show notification
                showNotification(context);
                return;
            }
        }

        //???
        if (!Utils.getInternalSettingBool(context, SHOULD_SHOW, true)) {
            Utils.setInternalSetting(context, SHOULD_SHOW, true);
        }
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
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
