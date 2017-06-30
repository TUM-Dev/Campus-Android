package de.tum.in.tumcampusapp.auxiliary;
import android.content.Context;
import android.content.SharedPreferences;
import de.tum.in.tumcampusapp.services.FavoriteDishAlarmScheduler;

/**
 * This class is responsible for keeping track of all the scheduled food alarms.
 * By instantiating a new entry, it will automatically be added to a hashmap, which
 * holds all the information about a given day's menus, which are marked as favorite.
 * If and only if the alarm wasn't already set, an alarm will be scheduled for its date.
 * This is achieved by instantiating a new instance of FavoriteDishAlarmScheduler, which constructs
 * a notification, when it's fired.
 */

public class FavoriteFoodAlarmStorage {
    private static FavoriteFoodAlarmStorage singleton;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;

    private FavoriteFoodAlarmStorage(){}
    public static FavoriteFoodAlarmStorage getInstance(Context context){
        if (singleton == null){
            singleton = new FavoriteFoodAlarmStorage();
        }
        FavoriteFoodAlarmStorage.context = context;
        sharedPreferences = context.getSharedPreferences("FavoriteFoodAlarmStorage", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        return singleton;
    }

    /**
     * Schedules an alarm at a given day, if there's not already an alarm scheduled for that day.
     * @param when
     */
    public void scheduleAlarm(String when){
        synchronized (singleton) {
            if (sharedPreferences.getBoolean(when, false)) {
                return;
            }
            editor.putBoolean(when, true);
            editor.commit();
            FavoriteDishAlarmScheduler favoriteDishAlarmScheduler = new FavoriteDishAlarmScheduler();
            favoriteDishAlarmScheduler.setFoodAlarm(context, when);
        }
    }

    /**
     * Does nothing if there's no alarm stored, otherwise cancels it
     * @param when
     */

    public void cancelAlarm(String when){
        synchronized (singleton) {
            if (!sharedPreferences.getBoolean(when, false)) {
                return;
            }
            editor.remove(when);
            editor.commit();
            FavoriteDishAlarmScheduler favoriteDishAlarmScheduler = new FavoriteDishAlarmScheduler();
            favoriteDishAlarmScheduler.cancelFoodAlarm(context, when);
        }
    }

    /**
     * Goes through all scheduled alarms and cancels them by calling cancelAlarm() on each of them.
     */
    public void cancelOutstandingAlarms(){
        synchronized (singleton){
            for(String when : sharedPreferences.getAll().keySet()){
                cancelAlarm(when);
            }
        }
    }
}
