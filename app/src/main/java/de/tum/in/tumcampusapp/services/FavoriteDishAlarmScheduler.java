package de.tum.in.tumcampusapp.services;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Pair;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.CafeteriaActivity;
import de.tum.in.tumcampusapp.auxiliary.CafeteriaNotificationSettings;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.CafeteriaManager;

/**
 * Alarm class for scheduling future favorite food notification.
 * To support backward compatibility, one notification is constructed per
 * found dish. This also ensures that tapping it shows the user the correct
 * cafeteria in the newly opened cafeteria activity. The alarm itself,
 * will launch at a given day and then consult the FavoriteFoodAlarmEntry's scheduledEntries
 * to find out whether there are still outstanding notifications at that specific day, or
 * if they've been canceled in the meantime. Depending on the result, the notification will
 * either be triggered or the alarm will do nothing.
 */

public class FavoriteDishAlarmScheduler extends BroadcastReceiver {
    private static Set<Integer> activeNotifications = Collections.synchronizedSet(new HashSet<Integer>());
    private static final String NOTIFICATION_TAG = "TCA_FAV_FOOD";
    public static final String INTENT_CANCEL_ALL_NOTIFICATIONS = "cancelNotifications";

    public FavoriteDishAlarmScheduler(){}
    public FavoriteDishAlarmScheduler(Calendar triggeredAt, Context context){
        Calendar scheduledAt = (Calendar) triggeredAt.clone();
        if (!loadTriggerHourAndMinute(context, scheduledAt)){
            return;
        }
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, FavoriteDishAlarmScheduler.class);
        intent.putExtra("triggeredAt", Utils.getDateString(triggeredAt.getTime()));
        PendingIntent schedule = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledAt.getTimeInMillis(), schedule);
    }

    /**
     * Can either receive a date or a boolean cancelNotifications value. This way other activities
     * can close the currently opened notifications and it is possible to schedule dates, where the
     * alarm has to check for favorite dishes.
     * @param context
     * @param extra
     * Extra can either be "cancelNotifications" or a date, when the alarm should check, if there are any
     * favorite dishes at a given date.
     */

    @Override
    public void onReceive(Context context, Intent extra) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        cancelFoodNotifications(mNotificationManager);
        if (extra.getBooleanExtra("cancelNotifications", false)){
            return;
        }
        String triggeredAt = extra.getStringExtra("triggeredAt");
        Calendar triggeredCal = Calendar.getInstance();
        triggeredCal.setTime(Utils.getDate(triggeredAt));
        HashSet<FavoriteFoodAlarmEntry> scheduledNow = FavoriteFoodAlarmEntry.getEntriesByCalendar(triggeredCal);
        if (scheduledNow == null){
            return;
        }
        CafeteriaManager cmm = new CafeteriaManager(context);

        int i=0;
        for (FavoriteFoodAlarmEntry fae : scheduledNow){
            activeNotifications.add(i);
            String mensaName = cmm.getMensaNameFromId(fae.getMensaId());
            Intent intent = new Intent(context, CafeteriaActivity.class);
            intent.putExtra(Const.MENSA_FOR_FAVORITEDISH, fae.getMensaId());
            PendingIntent pi = PendingIntent.getActivity(context, fae.getMensaId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String message = mensaName+"\n"+fae.getDishName();
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("TumCampusApp")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentText(message)
                    .setAutoCancel(true);
            mBuilder.setContentIntent(pi);
            mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            mBuilder.setAutoCancel(true);
            mNotificationManager.notify(NOTIFICATION_TAG,i, mBuilder.build());
            i++;
        }
    }

    private void cancelFoodNotifications(NotificationManager mNotificationManager){
        synchronized (activeNotifications){
            Iterator<Integer> it = activeNotifications.iterator();
            while (it.hasNext()){
                mNotificationManager.cancel(NOTIFICATION_TAG, it.next());
                it.remove();
            }
        }
    }

    /**
     * Checks if the user set / or disabled (hour =-1) an hour for a potential schedule.
     * @param context
     * @param scheduledAt
     * @return
     */

    private boolean loadTriggerHourAndMinute(Context context, Calendar scheduledAt){
        CafeteriaNotificationSettings cafeteriaNotificationSettings = new CafeteriaNotificationSettings(context);
        Pair<Integer, Integer> hourMinute = cafeteriaNotificationSettings.retrieveHourMinute(scheduledAt);
        if (hourMinute.first == -1){
            return false;
        }
        scheduledAt.set(Calendar.HOUR_OF_DAY, hourMinute.first);
        scheduledAt.set(Calendar.MINUTE, hourMinute.second);
        return true;
    }
}
