package de.tum.in.tumcampus.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;

/**
 * Created by m.hesse on 01.06.2015.
 */
public class SmartAlarmReceiver extends BroadcastReceiver {
    public static String PRE_ALARM = "PRE_ALARM";
    public static int PRE_ALARM_REQUEST = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra(PRE_ALARM, false)) {
            handlePreAlarm(context);
        } else {
            handleAlarm(context);
        }
    }

    private void handlePreAlarm(Context c) {
    }

    private void handleAlarm(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
    }
}
