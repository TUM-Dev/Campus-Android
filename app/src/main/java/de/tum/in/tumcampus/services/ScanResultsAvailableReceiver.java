package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.SetupEduroam;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.EduroamManager;

public class ScanResultsAvailableReceiver extends BroadcastReceiver {
    private static final String SHOULD_SHOW = "setup_notification_dismissed";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Test if user has eduroam configured already
        EduroamManager man = new EduroamManager(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean show = prefs.getBoolean("card_eduroam_phone", true);
        if(man.isConfigured() || Utils.isConnected(context) || Build.VERSION.SDK_INT<18 || !show)
            return;

        // Test if eduroam is available
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scan = wifi.getScanResults();
        for(ScanResult network : scan) {
            if(network.SSID.equals(EduroamManager.networkSSID)) {
                //Show notification
                showNotification(context);
                return;
            }
        }

        prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, 0);
        if(!prefs.getBoolean(SHOULD_SHOW, true)) {
            prefs.edit().putBoolean(SHOULD_SHOW, true).apply();
        }
    }

    public void showNotification(Context context) {
        // If previous notification is still visible
        SharedPreferences prefs = context.getSharedPreferences(Const.INTERNAL_PREFS, 0);
        if(!prefs.getBoolean(SHOULD_SHOW, true))
            return;

        // Prepate intents for notification actions
        Intent intent = new Intent(context, SetupEduroam.class);
        Intent hide = new Intent(context, NeverShowAgain.class);

        PendingIntent setupIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent hideIntent = PendingIntent.getService(context, 0, hide, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create Notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_wifi)
                .setTicker(context.getString(R.string.setup_eduroam))
                .setContentTitle(context.getString(R.string.setup_eduroam))
                .setContentText(context.getString(R.string.eduroam_setup_question))
                .addAction(R.drawable.ic_action_cancel, context.getString(R.string.not_ask_again), hideIntent)
                .addAction(R.drawable.ic_notification_wifi, context.getString(R.string.setup), setupIntent)
                .setContentIntent(setupIntent)
                .setAutoCancel(true);

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Build Notification with Notification Manager
        notificationmanager.notify(123, builder.build());

        prefs.edit().putBoolean(SHOULD_SHOW, false).apply();

    }

    public static class NeverShowAgain extends IntentService {

        private static final String NEVER_SHOW = "never_show";

        public NeverShowAgain() {
            super(NEVER_SHOW);
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean("card_eduroam_phone", false).apply();
        }
    }
}
