package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CalendarManager;

/**
 * Service used to silence the mobile during lectures
 */
public class SilenceService extends IntentService {

    static final ScheduledExecutorService tpe = Executors.newSingleThreadScheduledExecutor();
    static final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Interval in milliseconds to check for current lectures
     */
    private static final int CHECK_INTERVAL = 60000 * 15, // 15 Minutes
            CHECK_DELAY = 10000; // 10 Seconds after Calendar changed
    private static final String SILENCE_SERVICE = "SilenceService";
    
    private static int oldState = AudioManager.RINGER_MODE_NORMAL;

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
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }

        tpe.schedule(new Runnable() {
            @Override
            public void run() {
                long wait_duration = CHECK_INTERVAL;

                //Abort, if the settings changed
                if (!Utils.getSettingBool(SilenceService.this, Const.SILENCE_SERVICE, false)) {
                    isRunning.set(false);
                    return;
                }
                Utils.log("SilenceService enabled, checking for lectures ...");

                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                CalendarManager calendarManager = new CalendarManager(SilenceService.this);
                if (!calendarManager.hasLectures()) {
                    Utils.logv("No lectures available");
                    tpe.schedule(this, wait_duration, TimeUnit.MILLISECONDS);
                    return;
                }

                Cursor cursor = calendarManager.getCurrentFromDb();
                Utils.log("Current lectures: " + String.valueOf(cursor.getCount()));

                if (cursor.getCount() != 0) {
                    // remember old state if just activated ; in doubt dont change
                    if(!Utils.getInternalSettingBool(SilenceService.this, Const.SILENCE_ON, true))
                        oldState = am.getRingerMode();
                    
                    // if current lecture(s) found, silence the mobile
                    Utils.setInternalSetting(SilenceService.this, Const.SILENCE_ON, true);

                    // Set into silent mode
                    String mode = Utils.getSetting(SilenceService.this, "silent_mode_set_to", "0");
                    if (mode.equals("0")) {
                        Utils.log("set ringer mode: vibration");
                        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    } else {
                        Utils.log("set ringer mode: silent");
                        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    }
                    // refresh when event has ended
                    wait_duration = getWaitDuration(cursor.getString(3));
                } else if (Utils.getInternalSettingBool(SilenceService.this, Const.SILENCE_ON, false)) {
                    // default: old state
                    Utils.log("set ringer mode to old state");
                    am.setRingerMode(oldState);
                    Utils.setInternalSetting(SilenceService.this, Const.SILENCE_ON, false);


                    Cursor cursor2 = calendarManager.getNextCalendarItem();
                    // refresh when next event has started
                    wait_duration = getWaitDuration(cursor2.getString(1));
                    cursor2.close();
                }
                cursor.close();

                tpe.schedule(this, wait_duration, TimeUnit.MILLISECONDS);
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    private static long getWaitDuration(String timeToEventString) {
        long timeToEvent = Long.MAX_VALUE;
        try {
            timeToEvent = (new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(timeToEventString)).getTime();
        } catch (ParseException e) {
            Utils.log(e, "");
        }
        return Math.min(CHECK_INTERVAL, timeToEvent - System.currentTimeMillis() + CHECK_DELAY);
    }
}
