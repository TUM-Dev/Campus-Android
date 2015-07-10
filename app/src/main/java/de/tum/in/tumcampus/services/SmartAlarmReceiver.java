package de.tum.in.tumcampus.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.tum.in.tumcampus.auxiliary.AlarmSchedulerTask;
import de.tum.in.tumcampus.models.ConnectionToCampus;

public class SmartAlarmReceiver extends BroadcastReceiver {
    public static final String EST_WAKEUP_TIME = "EST_WAKEUP_TIME";
    public static final String PRE_ALARM = "PRE_ALARM";

    public static final int PRE_ALARM_REQUEST = 0;
    public static final int ALARM_REQUEST = 1;
    public static final String ROUTE = "ROUTE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra(PRE_ALARM, false)) {
            handlePreAlarm(context, intent);
        } else {
            handleAlarm(context);
        }
    }

    private void handlePreAlarm(Context c, Intent intent) {
        ConnectionToCampus ctc;
        if (intent.hasExtra(ROUTE)) {
            ctc = (ConnectionToCampus) intent.getExtras().get(ROUTE);
            new AlarmSchedulerTask(c, ctc, SmartAlarmReceiver.ALARM_REQUEST).execute();
        } else {
            new AlarmSchedulerTask(c, SmartAlarmReceiver.ALARM_REQUEST).execute();
        }
    }

    private void handleAlarm(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        // TODO: display alarm/info window above all content
        // TODO: display route info on screen and widget
        // TODO: play ringtone
        // TODO: vibrate if activated (add to prefs)
    }

    // TODO: add reboot handling (schedule alarm again)
    // TODO: add long time no lectures handling (schedule PreAlarmScheduler)
}
