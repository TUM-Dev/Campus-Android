package de.tum.in.tumcampus.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;

import de.tum.in.tumcampus.auxiliary.SmartAlarmUtils;

public class SmartAlarmReceiver extends BroadcastReceiver {
    public static final String EST_WAKEUP_TIME = "EST_WAKEUP_TIME";
    public static final String PRE_ALARM = "PRE_ALARM";

    public static final int PRE_ALARM_REQUEST = 0;
    public static final int ALARM_REQUEST = 1;
    public static final String ROUTE = "ROUTE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra(PRE_ALARM, false)) {
            handlePreAlarm(context);
        } else {
            handleAlarm(context);
        }
    }

    private void handlePreAlarm(Context c) {
        SmartAlarmUtils.scheduleAlarm(c);
    }

    private void handleAlarm(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
    }
}
