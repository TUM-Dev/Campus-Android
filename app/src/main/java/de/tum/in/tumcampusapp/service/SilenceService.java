package de.tum.in.tumcampusapp.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.JobIntentService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarController;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

import static de.tum.in.tumcampusapp.utils.Const.SILENCE_SERVICE_JOB_ID;

/**
 * Service used to silence the mobile during lectures
 */
public class SilenceService extends JobIntentService {

    /**
     * Interval in milliseconds to check for current lectures
     */
    private static final int CHECK_INTERVAL = 60000 * 15; // 15 Minutes
    private static final int CHECK_DELAY = 10000; // 10 Seconds after Calendar changed

    private static long getWaitDuration(String timeToEventString) {
        long timeToEvent = Long.MAX_VALUE;
        try {
            timeToEvent = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(timeToEventString)
                                                                                  .getTime();
        } catch (ParseException e) {
            Utils.log(e, "");
        }
        return Math.min(CHECK_INTERVAL, timeToEvent - System.currentTimeMillis() + CHECK_DELAY);
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

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SilenceService.class, SILENCE_SERVICE_JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //Abort, if the settings changed
        if (!Utils.getSettingBool(this, Const.SILENCE_SERVICE, false)) {
            // Don't schedule a new run, since the service is disabled
            return;
        }

        if (!hasPermissions(this)) {
            Utils.setSetting(this, Const.SILENCE_SERVICE, false);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        Intent newIntent = new Intent(this, SilenceService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long startTime = System.currentTimeMillis();
        long waitDuration = CHECK_INTERVAL;
        Utils.log("SilenceService enabled, checking for lectures …");

        CalendarController calendarController = new CalendarController(this);
        if (!calendarController.hasLectures()) {
            Utils.logv("No lectures available");
            alarmManager.set(AlarmManager.RTC, startTime + waitDuration, pendingIntent);
            return;
        }

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            return;
        }
        List<CalendarItem> currentLectures = calendarController.getCurrentFromDb();
        Utils.log("Current lectures: " + currentLectures.size());

        if (currentLectures.size() == 0 || isDoNotDisturbMode()) {
            if (Utils.getInternalSettingBool(this, Const.SILENCE_ON, false) && !isDoNotDisturbMode()) {
                // default: old state
                Utils.log("set ringer mode to old state");
                am.setRingerMode(Integer.parseInt(
                        Utils.getSetting(this, Const.SILENCE_OLD_STATE,
                                         Integer.toString(AudioManager.RINGER_MODE_NORMAL))));
                Utils.setInternalSetting(this, Const.SILENCE_ON, false);

                List<CalendarItem> nextCalendarItems = calendarController.getNextCalendarItems();
                if (nextCalendarItems.size() != 0) { //Check if we have a "next" item in the database and update the refresh interval until then. Otherwise use default interval.
                    // refresh when next event has started
                    waitDuration = getWaitDuration(nextCalendarItems.get(0)
                                                                    .getDtstart());
                }
            }
        } else {
            // remember old state if just activated ; in doubt dont change
            if (!Utils.getInternalSettingBool(this, Const.SILENCE_ON, true)) {
                Utils.setSetting(this, Const.SILENCE_OLD_STATE, am.getRingerMode());
            }

            // if current lecture(s) found, silence the mobile
            Utils.setInternalSetting(this, Const.SILENCE_ON, true);

            // Set into silent mode
            String mode = Utils.getSetting(this, "silent_mode_set_to", "0");
            if ("0".equals(mode)) {
                Utils.log("set ringer mode: vibration");
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            } else {
                Utils.log("set ringer mode: silent");
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
            // refresh when event has ended
            waitDuration = getWaitDuration(currentLectures.get(0)
                                                          .getDtstart());
        }

        alarmManager.set(AlarmManager.RTC, startTime + waitDuration, pendingIntent);
    }

    /**
     * We can't and won't change the ringermodes, if the device is in DoNotDisturb mode. DnD requires
     * explicit user interaction, so we are out of the game until DnD is off again
     */
    private boolean isDoNotDisturbMode() {
        // see https://stackoverflow.com/questions/31387137/android-detect-do-not-disturb-status
        //Settings.System.getInt(getContentResolver(), Settings.System.DO_NOT_DISTURB, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.app.NotificationManager nm = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            return (nm.getCurrentInterruptionFilter() != android.app.NotificationManager.INTERRUPTION_FILTER_ALL);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                int mode = Settings.Global.getInt(getContentResolver(), "zen_mode");
                return mode != 0;
            } catch (Settings.SettingNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Check if the app has the permissions to enable "Do Not Disturb".
     */
    public static boolean hasPermissions(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                 && !notificationManager.isNotificationPolicyAccessGranted());
    }

    /**
     * Request the "Do Not Disturb" permissions for android version >= N.
     */
    public static void requestPermissions(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (hasPermissions(context)) {
            return;
        }
        requestPermissionsSDK23(context);
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private static void requestPermissionsSDK23(Context context) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        context.startActivity(intent);
    }
}
