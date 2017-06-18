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

    private static boolean put(Calendar date, Context context, FavoriteFoodAlarmEntry favoriteFoodAlarmEntry){
        synchronized (scheduledEntries) {
            //Clear old entries
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY,0);
            for (Calendar calendar : scheduledEntries.keySet()){
                if (calendar.before(today)){
                    scheduledEntries.remove(calendar);
                }
            }

            int yearScheduled = date.get(Calendar.YEAR);
            int dayOfYearScheduled = date.get(Calendar.DAY_OF_YEAR);
            int hourScheduled = date.get(Calendar.HOUR_OF_DAY);
            int minuteScheduled = date.get(Calendar.MINUTE);

            today = Calendar.getInstance();
            int year = today.get(Calendar.YEAR);
            int dayOfYear = today.get(Calendar.DAY_OF_YEAR);

            CafeteriaNotificationSettings cfs = new CafeteriaNotificationSettings(context);
            Pair<Integer,Integer> preferredHourAndMinute = cfs.retrieveHourMinute(date);

            int inMinutesScheduled = hourScheduled*60+minuteScheduled;
            int inMinutesPreferred = preferredHourAndMinute.first*60+preferredHourAndMinute.second;
            //If entry is for today and past the preferred scheduling time cancel otherwise continue constructing an alarm
            if (yearScheduled == year && dayOfYear == dayOfYearScheduled && inMinutesScheduled >= inMinutesPreferred) {
                return false;
            }
            HashSet<FavoriteFoodAlarmEntry> alarmEntries;
            if (scheduledEntries.containsKey(date)) {
                alarmEntries = scheduledEntries.get(date);
            } else {
                alarmEntries = new HashSet<>();
            }
            int sizeBefore = alarmEntries.size();
            alarmEntries.add(favoriteFoodAlarmEntry);
            scheduledEntries.put(date, alarmEntries);
            new FavoriteDishAlarmScheduler(date, favoriteFoodAlarmEntry.getContext());
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
