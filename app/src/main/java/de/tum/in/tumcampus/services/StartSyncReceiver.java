package de.tum.in.tumcampus.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.tum.in.tumcampus.auxiliary.Const;
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

        // Set Alarm for next update, if background service is enabled
        final boolean backgroundServiceEnabled = Utils.getSettingBool(context, Const.BACKGROUND_MODE, false);
        if(backgroundServiceEnabled)
            setAlarm(context);

        // Start BackgroundService
        if (launch || backgroundServiceEnabled) {
            Utils.logv("Start background service...");
            Intent i = new Intent(context, BackgroundService.class);
            i.putExtra(Const.APP_LAUNCHES,launch);
            context.startService(i);
        }
    }

    private void setAlarm(Context context) {
        // Intent to call on alarm
        Intent intent = new Intent(context, StartSyncReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set alarm
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.set(AlarmManager.RTC, System.currentTimeMillis() + StartSyncReceiver.START_INTERVAL, pendingIntent);
    }
}