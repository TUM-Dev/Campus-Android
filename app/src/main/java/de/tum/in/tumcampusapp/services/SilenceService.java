package de.tum.in.tumcampusapp.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.CalendarManager;

/**
 * Service used to silence the mobile during lectures
 */
public class SilenceService extends IntentService {

    /**
     * Interval in milliseconds to check for current lectures
     */
    private static final int CHECK_INTERVAL = 60000 * 15; // 15 Minutes
    private static final int CHECK_DELAY = 10000; // 10 Seconds after Calendar changed
    private static final String SILENCE_SERVICE = "SilenceService";

    /**
     * default init (run intent in new thread)
     */
    public SilenceService() {
        super(SILENCE_SERVICE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log("SilenceService has started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.log("SilenceService has stopped");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = new Intent(this, SilenceService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long startTime = System.currentTimeMillis();
        long wait_duration = CHECK_INTERVAL;

        //Abort, if the settings changed
        if (!Utils.getSettingBool(this, Const.SILENCE_SERVICE, false)) {
            // Don't schedule a new run, since the service is disabled
            return;
        }
        Utils.log("SilenceService enabled, checking for lectures ...");

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        CalendarManager calendarManager = new CalendarManager(this);
        if (!calendarManager.hasLectures()) {
            Utils.logv("No lectures available");
            alarmManager.set(AlarmManager.RTC, startTime + wait_duration, pendingIntent);
            return;
        }

        Cursor cursor = calendarManager.getCurrentFromDb();
        Utils.log("Current lectures: " + String.valueOf(cursor.getCount()));

        if (cursor.getCount() != 0) {
            // remember old state if just activated ; in doubt dont change
            if (!Utils.getInternalSettingBool(this, Const.SILENCE_ON, true)) {
                Utils.setSetting(this, Const.SILENCE_OLD_STATE, am.getRingerMode());
            }

            // if current lecture(s) found, silence the mobile
            Utils.setInternalSetting(this, Const.SILENCE_ON, true);

            // Set into silent mode
            String mode = Utils.getSetting(this, "silent_mode_set_to", "0");
            if (mode.equals("0")) {
                Utils.log("set ringer mode: vibration");
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            } else {
                Utils.log("set ringer mode: silent");
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
            // refresh when event has ended
            cursor.moveToFirst();
            wait_duration = getWaitDuration(cursor.getString(3));
        } else if (Utils.getInternalSettingBool(this, Const.SILENCE_ON, false)) {
            // default: old state
            Utils.log("set ringer mode to old state");
            am.setRingerMode(Integer.parseInt(
                    Utils.getSetting(this, Const.SILENCE_OLD_STATE,
                            Integer.toString(AudioManager.RINGER_MODE_NORMAL))));
            Utils.setInternalSetting(this, Const.SILENCE_ON, false);


            Cursor cursor2 = calendarManager.getNextCalendarItem();
            if (cursor.getCount() != 0) { //Check if we have a "next" item in the database and update the refresh interval until then. Otherwise use default interval.
                // refresh when next event has started
                wait_duration = getWaitDuration(cursor2.getString(1));
            }
            cursor2.close();
        }
        cursor.close();

        alarmManager.set(AlarmManager.RTC, startTime + wait_duration, pendingIntent);
    }

    private static long getWaitDuration(String timeToEventString) {
        long timeToEvent = Long.MAX_VALUE;
        try {
            timeToEvent = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(timeToEventString).getTime();
        } catch (ParseException e) {
            Utils.log(e, "");
        }
        return Math.min(CHECK_INTERVAL, timeToEvent - System.currentTimeMillis() + CHECK_DELAY);
    }
}
