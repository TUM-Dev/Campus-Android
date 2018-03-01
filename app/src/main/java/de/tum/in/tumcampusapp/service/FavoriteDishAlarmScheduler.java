package de.tum.in.tumcampusapp.service;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationSettings;
import de.tum.in.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity;
import de.tum.in.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * AlarmNotification class for scheduling future favorite food notification.
 * To support backward compatibility, one notification is constructed per
 * found dish. This also ensures that tapping it shows the user the correct
 * cafeteria in the newly opened cafeteria activity. The alarm itself,
 * will launch at a given day and then consult the FavoriteFoodAlarmStorage's scheduledEntries
 * to find out whether there are still outstanding notifications at that specific day, or
 * if they've been canceled in the meantime. Depending on the result, the notification will
 * either be triggered or the alarm will do nothing.
 */
public class FavoriteDishAlarmScheduler extends BroadcastReceiver {
    private static final Set<Integer> ACTIVE_NOTIFICATIONS = Collections.synchronizedSet(new HashSet<Integer>());
    private static final String IDENTIFIER_STRING = "TCA_FAV_FOOD";

    public void setFoodAlarm(Context context, String dateString) {
        Calendar scheduledAt = loadTriggerHourAndMinute(context, dateString);
        Calendar today = Calendar.getInstance();
        if (scheduledAt == null) {
            return;
        }
        if (today.after(scheduledAt)) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent schedule = constructAlarmIntent(context, dateString);
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, scheduledAt.getTimeInMillis(), 1000, schedule);
    }

    public void cancelFoodAlarm(Context context, String dateString) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(constructAlarmIntent(context, dateString));
    }

    /**
     * Generates a pending intent for a future alarm at a given date
     */
    private PendingIntent constructAlarmIntent(Context context, String dateString) {
        Intent intent = new Intent(context, FavoriteDishAlarmScheduler.class);
        intent.putExtra("triggeredAt", dateString);
        intent.setAction(IDENTIFIER_STRING);
        return PendingIntent.getBroadcast(context, dateString.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Can either receive a date or a boolean cancelNotifications value. This way other activities
     * can close the currently opened notifications and it is possible to schedule dates, where the
     * alarm has to check for favorite dishes.
     *
     * @param context
     * @param extra   Extra can either be "cancelNotifications" or a date, when the alarm should check, if there are any
     *                favorite dishes at a given date.
     */
    @Override
    public void onReceive(Context context, Intent extra) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        cancelFoodNotifications(mNotificationManager);
        if (extra.getBooleanExtra("cancelNotifications", false)) {
            return;
        }
        String triggeredAt = extra.getStringExtra("triggeredAt");
        Calendar triggeredCal = Calendar.getInstance();
        triggeredCal.setTime(DateUtils.getDate(triggeredAt));
        CafeteriaMenuManager cm = new CafeteriaMenuManager(context);
        HashMap<Integer, HashSet<CafeteriaMenu>> scheduledNow = cm.getServedFavoritesAtDate(triggeredAt);
        if (scheduledNow == null) {
            Utils.log("FavoriteDishAlarmScheduler: Scheduled now is null, onReceived aborted");
            return;
        }
        CafeteriaDao dao = TcaDb.getInstance(context)
                                .cafeteriaDao();
        for (Integer mensaId : scheduledNow.keySet()) {
            StringBuilder message = new StringBuilder();
            int menuCount = 0;
            for (CafeteriaMenu menu : scheduledNow.get(mensaId)) {
                message.append(menu.getName())
                       .append('\n');
                menuCount++;
            }
            ACTIVE_NOTIFICATIONS.add(mensaId);
            String mensaName = dao.getMensaNameFromId(mensaId);
            Intent intent = new Intent(context, CafeteriaActivity.class);
            intent.putExtra(Const.MENSA_FOR_FAVORITEDISH, mensaId);
            PendingIntent pi = PendingIntent.getActivity(context, mensaId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_CAFETERIA)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(mensaName + (menuCount > 1 ? " (" + menuCount + ")" : ""))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message.toString()))
                    .setContentText(message.toString())
                    .setAutoCancel(true)
                    .setLargeIcon(Utils.getLargeIcon(context, R.drawable.ic_cutlery))
                    .setContentIntent(pi)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true);
            mNotificationManager.notify(IDENTIFIER_STRING, mensaId, mBuilder.build());
        }
    }

    private void cancelFoodNotifications(NotificationManager mNotificationManager) {
        synchronized (ACTIVE_NOTIFICATIONS) {
            Iterator<Integer> it = ACTIVE_NOTIFICATIONS.iterator();
            while (it.hasNext()) {
                mNotificationManager.cancel(IDENTIFIER_STRING, it.next());
                it.remove();
            }
        }
    }

    /**
     * Checks if the user set / or disabled (hour =-1) an hour for a potential schedule.
     */
    private Calendar loadTriggerHourAndMinute(Context context, String dateString) {
        CafeteriaNotificationSettings cafeteriaNotificationSettings = new CafeteriaNotificationSettings(context);
        Calendar scheduledAt = Calendar.getInstance();
        scheduledAt.setTime(DateUtils.getDate(dateString));
        Pair<Integer, Integer> hourMinute = cafeteriaNotificationSettings.retrieveHourMinute(scheduledAt);
        if (hourMinute == null || hourMinute.first == -1) {
            return null;
        }
        scheduledAt.set(Calendar.HOUR_OF_DAY, hourMinute.first);
        scheduledAt.set(Calendar.MINUTE, hourMinute.second);
        return scheduledAt;
    }
}
