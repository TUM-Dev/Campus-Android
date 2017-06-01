package de.tum.in.tumcampusapp.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Calendar;

/**
 * Alarm class for scheduling a favorite food notification. Doesn't do anything after LATEST_SCHEDULE_HOUR
 *, because scheduling will be done daily.
 */
public class FavoriteDishAlarm extends BroadcastReceiver {
    private static final String BUNDLE_NAME = "FavoriteDishBundle";
    private static final int LATEST_SCHEDULE_HOUR = 14;
    private static Calendar lastSchedule = null;
    public FavoriteDishAlarm(Context context, Bundle extras, int hour, int minute){
        Calendar nextSchedule = Calendar.getInstance();
        nextSchedule.set(Calendar.HOUR_OF_DAY, hour);
        nextSchedule.set(Calendar.MINUTE, minute);
        if (isValidSchedule(nextSchedule)){
            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, FavoriteDishService.class);
            intent.putExtra(BUNDLE_NAME, extras);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, nextSchedule.getTimeInMillis(), pendingIntent);
            lastSchedule = nextSchedule;
        }
    }

    /**
     * Checks whether a FavoriteDishService should be scheduled, depending on the time of the day and
     * the time of the last schedule.
     * @return True, if the schedule happens before the LATEST_SCHEDULE_HOUR and a new day began, since
     * the last schedule. Otherwise false.
     */
    private boolean isValidSchedule(Calendar nextScheduleProposal){
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.HOUR_OF_DAY) > LATEST_SCHEDULE_HOUR
            || nextScheduleProposal.get(Calendar.HOUR_OF_DAY) > LATEST_SCHEDULE_HOUR) return false;
        if (lastSchedule == null) return true;
        if (nextScheduleProposal.after(lastSchedule) &&
           (lastSchedule.get(Calendar.DAY_OF_WEEK) != nextScheduleProposal.get(Calendar.DAY_OF_WEEK))){
            return true;
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, FavoriteDishService.class);
        context.startService(i);
    }
}
