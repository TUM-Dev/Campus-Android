package de.tum.in.tumcampusapp.services;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.CafeteriaActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.CafeteriaManager;

/**
 * Alarm class for scheduling future favorite food notification.
 */

public class FavoriteDishAlarmScheduler extends BroadcastReceiver {
    private static Set<Integer> activeNotifications = Collections.synchronizedSet(new HashSet<Integer>());
    private static final String NOTIFICATION_TAG = "TCA_FAV_FOOD";

    public FavoriteDishAlarmScheduler(){}
    public FavoriteDishAlarmScheduler(Calendar triggeredAt, Context context){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, FavoriteDishAlarmScheduler.class);
        intent.putExtra("triggeredAt", Utils.getDateString(triggeredAt.getTime()));
        PendingIntent schedule = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5000, schedule);
    }

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
}
