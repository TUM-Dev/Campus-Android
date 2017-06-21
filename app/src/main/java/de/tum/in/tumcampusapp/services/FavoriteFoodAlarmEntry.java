package de.tum.in.tumcampusapp.services;
import android.content.Context;
import android.support.v4.util.Pair;

import java.util.Calendar;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import de.tum.in.tumcampusapp.auxiliary.CafeteriaNotificationSettings;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * This class is responsible for keeping track of all the scheduled food alarms.
 * By instantiating a new entry, it will automatically be added to a hashmap, which
 * holds all the information about a given day's menus, which are marked as favorite.
 * If and only if the alarm wasn't already set, an alarm will be scheduled for its date.
 * This is achieved by instantiating a new instance of FavoriteDishAlarmScheduler, which constructs
 * a notification, when it's fired.
 */

public class FavoriteFoodAlarmEntry{
    private static ConcurrentHashMap<Calendar, HashSet<FavoriteFoodAlarmEntry>> scheduledEntries = new ConcurrentHashMap<>();
    private int mensaId;
    private String dishName;
    private Context context;

    public FavoriteFoodAlarmEntry(int mensaId, String dishName, Calendar date, Context context){
        this.mensaId = mensaId;
        this.dishName = dishName;
        this.context = context;
        put(date, context, this);
    }

    /**
     * @param date
     * date as day, month, year. all other values like minutes, seconds etc. have to be zero
     * @param context
     * @param favoriteFoodAlarmEntry
     * @return
     */

    private static boolean put(Calendar dateDayMonthYear, Context context, FavoriteFoodAlarmEntry favoriteFoodAlarmEntry){
        synchronized (scheduledEntries) {
            Calendar today = Calendar.getInstance();
            today.setTime(Utils.getDate(Utils.getDateString(today.getTime())));
            //Dont add entries which are from yesterday or older
            if (dateDayMonthYear.before(today)){
                return false;
            }
            //Clear all entries added in the past
            for (Calendar calendar : scheduledEntries.keySet()){
                if (calendar.before(today)){
                    scheduledEntries.remove(calendar);
                }
            }

            int yearScheduled = dateDayMonthYear.get(Calendar.YEAR);
            int dayOfYearScheduled = dateDayMonthYear.get(Calendar.DAY_OF_YEAR);
            today = Calendar.getInstance();
            int year = today.get(Calendar.YEAR);
            int dayOfYear = today.get(Calendar.DAY_OF_YEAR);
            int currentMinutes = today.get(Calendar.HOUR_OF_DAY)*60+today.get(Calendar.MINUTE);

            //if the dish is served today
            if (year == yearScheduled && dayOfYear == dayOfYearScheduled){
                //Get the user preferred time for this alarm
                CafeteriaNotificationSettings cfs = new CafeteriaNotificationSettings(context);
                Pair<Integer,Integer> preferredHourAndMinute = cfs.retrieveHourMinute(dateDayMonthYear);
                int inMinutesPreferred = preferredHourAndMinute.first*60+preferredHourAndMinute.second;
                //And if it's already later than the preferred time, dont add the alarm
                //This is necessary, because alarms scheduled in the past fire instantly
                if (currentMinutes >= inMinutesPreferred){
                    return false;
                }
            }
            //
            HashSet<FavoriteFoodAlarmEntry> alarmEntries;
            /*if there's already a calendar entry for a scheduled alarm, e.g. by another dish served
             at the same date, append to its dishlist otherwise create a new dishlist and add the current
             dish to it
             */
            if (scheduledEntries.containsKey(dateDayMonthYear)) {
                alarmEntries = scheduledEntries.get(dateDayMonthYear);
            } else {
                alarmEntries = new HashSet<>();
            }
            //Check if an actual entry was made
            int sizeBefore = alarmEntries.size();
            alarmEntries.add(favoriteFoodAlarmEntry);
            scheduledEntries.put(dateDayMonthYear, alarmEntries);
            new FavoriteDishAlarmScheduler(dateDayMonthYear, favoriteFoodAlarmEntry.getContext());
            return (alarmEntries.size() > sizeBefore);
        }
    }

    public static void removeAll(){
        scheduledEntries.clear();
        Utils.log("Alarm scheduled food cleared");
    }

    private Context getContext(){
        return context;
    }

    public int getMensaId(){
        return mensaId;
    }

    public String getDishName(){
        return dishName;
    }

    public static HashSet<FavoriteFoodAlarmEntry> getEntriesByCalendar(Calendar triggeredAt){
        synchronized (scheduledEntries){
            if(scheduledEntries.containsKey(triggeredAt)){
                return scheduledEntries.get(triggeredAt);
            }else{
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return "FavoriteFoodAlarmEntry{" +
                "mensaId=" + mensaId +
                ", dishName='" + dishName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteFoodAlarmEntry that = (FavoriteFoodAlarmEntry) o;
        if (mensaId != that.mensaId) return false;
        return dishName != null ? dishName.equals(that.dishName) : that.dishName == null;
    }

    @Override
    public int hashCode() {
        int result = mensaId;
        result = 31 * result + (dishName != null ? dishName.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }
}
