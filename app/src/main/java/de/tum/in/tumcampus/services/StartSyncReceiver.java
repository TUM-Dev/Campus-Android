package de.tum.in.tumcampus.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Receives on boot completed broadcast, sets alarm for next sync try
 * and start BackgroundService if enabled in settings
 * */
public class StartSyncReceiver extends BroadcastReceiver {
    public static final String FORCE_START_SERVICE = "force_start_service";
    private static final long START_INTERVAL = AlarmManager.INTERVAL_HOUR * 3;

    @Override
    public void onReceive(Context context, Intent intent) {
        // check intent if called from StartupActivity
        final boolean force = intent.getBooleanExtra(FORCE_START_SERVICE, false);

        // Set Alarm for next update, if background service is enabled
        final boolean backgroundServiceEnabled = Utils.getSettingBool(context, Const.BACKGROUND_MODE);
        if(backgroundServiceEnabled)
            setAlarm(context, START_INTERVAL);

        // Start BackgroundService
        if(force || backgroundServiceEnabled) {
            Log.d("SyncReceiver", "Start background service...");
            context.startService(new Intent(context, BackgroundService.class));
        }
    }

    private void setAlarm(Context context, long time) {
        // Intent to call on alarm
        Intent intent = new Intent(context, StartSyncReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set alarm
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.set(AlarmManager.RTC, System.currentTimeMillis() + time, pendingIntent);
    }
}