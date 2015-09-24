package de.tum.in.tumcampus.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Receives on boot completed broadcast, sets alarm for next sync-try
 * and start BackgroundService if enabled in settings
 * */
public class StartSyncReceiver extends BroadcastReceiver {
    private static final long START_INTERVAL = AlarmManager.INTERVAL_HOUR * 3;

    @Override
    public void onReceive(Context context, Intent intent) {
        // check intent if called from StartupActivity
        final boolean launch = intent.getBooleanExtra(Const.APP_LAUNCHES, false);

        // Look up background service settings
        final boolean backgroundServicePermitted = isBackgroundServicePermitted(context);

        // Set Alarm for next update, if background service is enabled
        if(backgroundServicePermitted)
            setAlarm(context);

        // Start BackgroundService
        if (launch || backgroundServicePermitted) {
            Utils.logv("Start background service...");
            Intent i = new Intent(context, BackgroundService.class);
            i.putExtra(Const.APP_LAUNCHES,launch);
            context.startService(i);
        }

        context.startService(new Intent(context, SendMessageService.class));
    }

    private static boolean isBackgroundServicePermitted(Context context) {
        return isBackgroundServiceEnabled(context) && (isBackgroundServiceAlwaysEnabled(context) || NetUtils.isConnectedWifi(context));
    }

    private static boolean isBackgroundServiceEnabled(Context context) {
        return Utils.getSettingBool(context, Const.BACKGROUND_MODE, false);
    }

    private static boolean isBackgroundServiceAlwaysEnabled(Context context) {
        return Utils.getSetting(context, "background_mode_set_to", "0").equals("0");
    }

    private static void setAlarm(Context context) {
        // Intent to call on alarm
        Intent intent = new Intent(context, StartSyncReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set alarm
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.set(AlarmManager.RTC, System.currentTimeMillis() + StartSyncReceiver.START_INTERVAL, pendingIntent);
    }
}