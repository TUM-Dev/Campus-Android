package de.tum.in.tumcampusapp.auxiliary;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;
import android.support.v7.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.Calendar;
import de.tum.in.tumcampusapp.managers.CafeteriaMenuManager;

public class CafeteriaNotificationSettings {
    private SharedPreferences sharedPreferences;
    private final String PREFIX = "CAFETERIA_SCHEDULE_";
    private final String MINUTE = "_MINUTE";
    private final String HOUR = "_HOUR";
    private CafeteriaMenuManager cafeteriaMenuManager;

    public CafeteriaNotificationSettings(Context context){
        cafeteriaMenuManager = new CafeteriaMenuManager(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar it = Calendar.getInstance();
        it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        boolean wasAlreadyInitialized = true;
        while (it.get(Calendar.DAY_OF_WEEK) < Calendar.SATURDAY){
            if (writeDayToSettings(it, 9, 30, false)){
                wasAlreadyInitialized = false;
            }
            it.add(Calendar.DAY_OF_WEEK, 1);
        }
        if (!wasAlreadyInitialized){
            cafeteriaMenuManager.scheduleFoodAlarms(true);
        }
    }

    private void writeDayToSettings(Calendar weekday, int hour, int minute){
        int dayOfWeek = weekday.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREFIX+dayOfWeek+HOUR, hour);
            editor.putInt(PREFIX+dayOfWeek+MINUTE, minute);
            editor.commit();
        }
    }

    private boolean writeDayToSettings(Calendar weekday, int hour, int minute, boolean overwrite){
        Pair<Integer, Integer> hourMinuteStored = retrieveHourMinute(weekday);
        if (hourMinuteStored == null) {
            writeDayToSettings(weekday, hour, minute);
        }else if (overwrite && (hourMinuteStored.first != hour || hourMinuteStored.second != minute)) {
            writeDayToSettings(weekday, hour, minute);
        }
        else{
            return false;
        }
        return true;
    }

    public Pair<Integer,Integer> retrieveHourMinute(Calendar day){
        int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
            int hour = sharedPreferences.getInt(PREFIX+dayOfWeek+HOUR, -2);
            int minute = sharedPreferences.getInt(PREFIX+dayOfWeek+MINUTE, -2);
            if (hour == -2 || minute == -2) return null;
            return new Pair<>(hour,minute);
        }
        return null;
    }

    private boolean updateHourMinuteOfDay(Calendar weekday, int hour, int minute){
        Pair<Integer, Integer> currentlyStored = retrieveHourMinute(weekday);
        if (currentlyStored != null && currentlyStored.first == hour && currentlyStored.second == minute){
            return false;
        }
        if ((hour == -1 && minute == -1) || (hour > 0 && hour < 15 && minute >= 0 && minute < 60)){
            return (writeDayToSettings(weekday, hour, minute, true));
        }
        return false;
    }

    public boolean saveWholeSchedule(ArrayList<Pair<Integer,Integer>> mondayToFriday){
        if (mondayToFriday == null || mondayToFriday.size() != 5){
            return false;
        }else{
            Calendar monday = Calendar.getInstance();
            boolean storedSomething = false;
            for (int i=0; i < mondayToFriday.size(); i++){
                monday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                monday.add(Calendar.DAY_OF_WEEK, i);
                if (updateHourMinuteOfDay(monday, mondayToFriday.get(i).first, mondayToFriday.get(i).second)){
                    storedSomething = true;
                }
            }
            if (storedSomething){
                cafeteriaMenuManager.scheduleFoodAlarms(true);
                return true;
            }
            return false;
        }
    }
}
