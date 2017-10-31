package de.tum.in.tumcampusapp.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.WifiMeasurementManager;

/**
 * Receives on boot completed broadcast, sets alarm for next sync-try
 * and start BackgroundService if enabled in settings
 */
public class StartSyncReceiver extends BroadcastReceiver {
    private static final long START_INTERVAL = AlarmManager.INTERVAL_HOUR * 3;

    private static void setAlarm(Context context) {
        // Intent to call on alarm
        Intent intent = new Intent(context, StartSyncReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set alarm
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        if (Build.VERSION.SDK_INT < 19) {
            alarm.set(AlarmManager.RTC, System.currentTimeMillis() + StartSyncReceiver.START_INTERVAL, pendingIntent);
        } else {
            alarm.setExact(AlarmManager.RTC, System.currentTimeMillis() + StartSyncReceiver.START_INTERVAL, pendingIntent);
        }
    }

    @Override
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        // check intent if called from StartupActivity
        final boolean launch = intent.getBooleanExtra(Const.APP_LAUNCHES, false);

        // Look up background service settings
        final boolean backgroundServicePermitted = Utils.isBackgroundServicePermitted(context);

        // Set Alarm for next update, if background service is enabled
        if (backgroundServicePermitted) {
            setAlarm(context);
        }

        // Start BackgroundService
        if (launch || backgroundServicePermitted) {
            Utils.logv("Start background service...");
            Intent i = new Intent(context, BackgroundService.class);
            i.putExtra(Const.APP_LAUNCHES, launch);
            context.startService(i);
        }

        context.startService(new Intent(context, SendMessageService.class));

        // Also start the SilenceService. It checks if it is enabled, so we don't need to
        context.startService(new Intent(context, SilenceService.class));
        if (intent.getAction() != "android.net.wifi.WIFI_STATE_CHANGED" && Utils.getInternalSettingBool(context, WifiMeasurementManager.WIFI_SCANS_ALLOWED, false)) {
            context.startService(new Intent(context, SendWifiMeasurementService.class));
        }
    }
}