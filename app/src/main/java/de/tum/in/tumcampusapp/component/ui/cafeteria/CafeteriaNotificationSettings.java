package de.tum.in.tumcampusapp.component.ui.cafeteria;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;
import android.support.v7.preference.PreferenceManager;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.ReadableDateTime;

import java.util.ArrayList;
import java.util.Calendar;

import de.tum.in.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager;

/**
 * This class takes care of saving a user's preferred time for notifying him about a cafeteria's
 * servings at a given day.
 */
public class CafeteriaNotificationSettings {
    private final SharedPreferences sharedPreferences;
    private final String PREFIX = "CAFETERIA_SCHEDULE_";
    private final String MINUTE = "_MINUTE";
    private final String HOUR = "_HOUR";
    private final CafeteriaMenuManager cafeteriaMenuManager;

    /**
     * Checks if there's already a preferred notification time for every weekday(Monday-Friday)
     * and for every day missing it defaults to DEFAULT_HOUR:DEFAULT_MINUTE
     */
    public CafeteriaNotificationSettings(Context context) {
        cafeteriaMenuManager = new CafeteriaMenuManager(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        DateTime it = DateTime.now()
                .withDayOfWeek(DateTimeConstants.MONDAY);
        boolean wasAlreadyInitialized = true;
        while (it.getDayOfWeek() < DateTimeConstants.SATURDAY) {
            if (writeDayToSettings(it, 9, 30, false)) {
                wasAlreadyInitialized = false;
            }
            it = it.plusWeeks(1);
        }
        if (!wasAlreadyInitialized) {
            cafeteriaMenuManager.scheduleFoodAlarms(true);
        }
    }

    /**
     * If weekday is in range [Monday, Friday], set the preferred notification time for weekday
     * to hour:minute
     */
    private void writeDayToSettings(ReadableDateTime weekday, int hour, int minute) {
        int dayOfWeek = weekday.getDayOfWeek();
        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREFIX + dayOfWeek + HOUR, hour);
            editor.putInt(PREFIX + dayOfWeek + MINUTE, minute);
            editor.apply();
        }
    }

    /**
     * If weekday is in range [Monday, Friday], set the preferred notification time for weekday,
     * but only if, its either not set yet, or overwrite is set to true and the new hour:minute pair
     * differs from the one currently set
     */
    private boolean writeDayToSettings(ReadableDateTime weekday, int hour, int minute, boolean overwrite) {
        Pair<Integer, Integer> hourMinuteStored = retrieveHourMinute(weekday);
        if (hourMinuteStored == null) {
            writeDayToSettings(weekday, hour, minute);
        } else if (overwrite && (hourMinuteStored.first != hour || hourMinuteStored.second != minute)) {
            writeDayToSettings(weekday, hour, minute);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Returns the currently stored preferred hour and minute of a given day of the week.
     *
     * @param day A day from the interval [MONDAY, FRIDAY]
     */

    public Pair<Integer, Integer> retrieveHourMinute(ReadableDateTime day) {
        int dayOfWeek = day.getDayOfWeek();
        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
            int hour = sharedPreferences.getInt(PREFIX + dayOfWeek + HOUR, -2);
            int minute = sharedPreferences.getInt(PREFIX + dayOfWeek + MINUTE, -2);
            if (hour == -2 || minute == -2) {
                return null;
            }
            return new Pair<>(hour, minute);
        }
        return null;
    }

    /**
     * Checks if there's already a time preference for a weekday or not. The settingsPrefix
     * only differ by DAY_OF_WEEK.
     *
     * @param weekday A day ranging from MONDAY to FRIDAY
     * @param hour    The preferred hour for a notification
     * @param minute  The preferred minute for a notification
     */
    private boolean updateHourMinuteOfDay(DateTime weekday, int hour, int minute) {
        Pair<Integer, Integer> currentlyStored = retrieveHourMinute(weekday);
        if (currentlyStored != null && currentlyStored.first == hour && currentlyStored.second == minute) {
            return false;
        }
        return ((hour == -1 && minute == -1) || (hour >= 0 && hour < 24 && minute >= 0 && minute < 60)) && (writeDayToSettings(weekday, hour, minute, true));
    }

    /**
     * Stores all the preferred notification times from monday to friday.
     *
     * @param mondayToFriday An arraylist of hour-minute pairs ordered from monday to friday.
     * @return True if actual changes occured, False if nothing really changed.
     */
    public boolean saveWholeSchedule(ArrayList<Pair<Integer, Integer>> mondayToFriday) {
        if (mondayToFriday == null || mondayToFriday.size() != 5) {
            return false;
        } else {
            DateTime monday = DateTime.now();
            boolean storedSomething = false;
            for (int i = 0; i < mondayToFriday.size(); i++) {
                monday = monday.plusWeeks(1)
                        .withDayOfWeek(DateTimeConstants.MONDAY);
                if (updateHourMinuteOfDay(monday, mondayToFriday.get(i).first, mondayToFriday.get(i).second)) {
                    storedSomething = true;
                }
            }
            if (storedSomething) {
                cafeteriaMenuManager.scheduleFoodAlarms(true);
                return true;
            }
            return false;
        }
    }
}
