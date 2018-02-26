package de.tum.in.tumcampusapp.component.ui.cafeteria;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;
import android.support.v7.preference.PreferenceManager;

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

    //Used for initializing preferred hour for every weekday
    private static final int DEFAULT_HOUR = 9;
    //Used for initializing preferred minute for every weekday
    private static final int DEFAULT_MINUTE = 30;

    /**
     * Checks if there's already a preferred notification time for every weekday(Monday-Friday)
     * and for every day missing it defaults to DEFAULT_HOUR:DEFAULT_MINUTE
     */
    public CafeteriaNotificationSettings(Context context) {
        cafeteriaMenuManager = new CafeteriaMenuManager(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar it = Calendar.getInstance();
        it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        boolean wasAlreadyInitialized = true;
        while (it.get(Calendar.DAY_OF_WEEK) < Calendar.SATURDAY) {
            if (writeDayToSettings(it, 9, 30, false)) {
                wasAlreadyInitialized = false;
            }
            it.add(Calendar.DAY_OF_WEEK, 1);
        }
        if (!wasAlreadyInitialized) {
            cafeteriaMenuManager.scheduleFoodAlarms(true);
        }
    }

    /**
     * If weekday is in range [Monday, Friday], set the preferred notification time for weekday
     * to hour:minute
     *
     * @param weekday
     * @param hour
     * @param minute
     */
    private void writeDayToSettings(Calendar weekday, int hour, int minute) {
        int dayOfWeek = weekday.get(Calendar.DAY_OF_WEEK);
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
     *
     * @param hour
     * @param minute
     */
    private boolean writeDayToSettings(Calendar weekday, int hour, int minute, boolean overwrite) {
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
     * @return
     */

    public Pair<Integer, Integer> retrieveHourMinute(Calendar day) {
        int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
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
     * Checks if there's already a time preference for a weekday or not. The settings
     * only differ by DAY_OF_WEEK.
     *
     * @param weekday A day ranging from MONDAY to FRIDAY
     * @param hour    The preferred hour for a notification
     * @param minute  The preferred minute for a notification
     * @return
     */
    private boolean updateHourMinuteOfDay(Calendar weekday, int hour, int minute) {
        Pair<Integer, Integer> currentlyStored = retrieveHourMinute(weekday);
        if (currentlyStored != null && currentlyStored.first == hour && currentlyStored.second == minute) {
            return false;
        }
        if ((hour == -1 && minute == -1) || (hour >= 0 && hour < 24 && minute >= 0 && minute < 60)) {
            return (writeDayToSettings(weekday, hour, minute, true));
        }
        return false;
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
            Calendar monday = Calendar.getInstance();
            boolean storedSomething = false;
            for (int i = 0; i < mondayToFriday.size(); i++) {
                monday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                monday.add(Calendar.DAY_OF_WEEK, i);
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
