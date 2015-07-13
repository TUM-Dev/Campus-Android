package de.tum.in.tumcampus.auxiliary;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.CalendarRow;
import de.tum.in.tumcampus.models.SmartAlarmInfo;
import de.tum.in.tumcampus.models.Geo;
import de.tum.in.tumcampus.models.LectureAppointmentsRow;
import de.tum.in.tumcampus.services.SmartAlarmReceiver;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampus.widget.SmartAlarmWidgetProvider;

public class SmartAlarmUtils {
    public static final long MINUTEINMS = 60 * 1000;
    public static final long HOURINMS = 60 * MINUTEINMS;
    public static final long DAYINMS = 24 * HOURINMS;

    public static void scheduleAlarm(Context c) {
        new AlarmSchedulerTask(c).execute();
    }


    public static void scheduleAlarmWithProgressDialog(Context c, ProgressDialog pd) {
        new AlarmSchedulerTask(c, pd).execute();
    }

    public static void cancelAlarm(Context c) {
        PendingIntent pi = PendingIntent.getBroadcast(c, 0, new Intent(c, SmartAlarmReceiver.class), 0);
        ((AlarmManager) c.getSystemService(Service.ALARM_SERVICE)).cancel(pi);
        pi.cancel();
    }

    public static void reSchedulePreAlarm(Context c, ProgressDialog pd) {
        cancelAlarm(c);
        scheduleAlarmWithProgressDialog(c, pd);
    }

    public static SmartAlarmInfo calculateJourney(Context c, SmartAlarmInfo ctc, long arrivalAtCampus) {
        return calculateJourney(c, ctc.getFromStation(), ctc.getToPosition(), arrivalAtCampus);
    }

    public static SmartAlarmInfo calculateJourney(Context c, int fromStationId, String toStreet, long arrivalAtCampus) {
        MVGRequest mvgRequest = new MVGRequest(c);
        try {
            Geo toPos = mvgRequest.fetchStreetPos(toStreet);
            return calculateJourney(c, fromStationId, toPos, arrivalAtCampus);
        } catch (Exception e) {
            Utils.log(e);
        }
        return null;
    }

    public static SmartAlarmInfo calculateJourney(Context c, int fromStation, Geo toPosition, long arrivalAtCampus) {
        MVGRequest mvgRequest = new MVGRequest(c);
        try {
            return new SmartAlarmInfo(mvgRequest.fetchRouteArrivingAt(fromStation, toPosition.getLatitude(), toPosition.getLongitude(), arrivalAtCampus), arrivalAtCampus);
        } catch (Exception e) {
            Utils.log(e);
        }
        return null;
    }

    public static <T> T safeFetch(TUMOnlineRequest<T> tor) {
        try {
            return tor.fetch();
        } catch (Exception e) {
            Utils.log(e.getMessage());
            Utils.log(e);
            return null;
        }
    }

    static void retryLater(Context c, int hours) {
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Service.ALARM_SERVICE);
        Intent i = new Intent(c, SmartAlarmReceiver.class);
        i.setAction(SmartAlarmReceiver.ACTION_RETRY);
        PendingIntent pi = PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + hours * HOURINMS, pi);

        Utils.log("SmartAlarm: scheduled " + SmartAlarmReceiver.ACTION_RETRY + " at "
                + android.text.format.DateUtils.formatDateTime(c, System.currentTimeMillis() + hours * HOURINMS, android.text.format.DateUtils.FORMAT_SHOW_DATE) + " "
                + android.text.format.DateUtils.formatDateTime(c, System.currentTimeMillis() + hours * HOURINMS, android.text.format.DateUtils.FORMAT_SHOW_TIME));
    }

    public static void retryWithInfo(Context c, String message) {
        retryLater(c, AlarmSchedulerTask.RETRY_NOINTERNET_INTERVAL);
        showError(c, c.getString(R.string.smart_alarm_connection_error), message, c.getString(R.string.smart_alarm_retry));
    }

    public static void retryWithError(Context c, String message, int hours) {
        retryLater(c, hours);
        showError(c, c.getString(R.string.smart_alarm_disabled_next_lecture), message, c.getString(R.string.smart_alarm_set_own_clock));
    }

    public static void disableWithError(Context c, String message) {
        showError(c, c.getString(R.string.smart_alarm_disabled), message, "");

        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(c).edit();
        e.putBoolean("smart_alarm_active", false);
        e.apply();

        updateWidget(c, null, false);
    }

    public static void showError(Context c, String title, String message, String longMessage) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_launcher))
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message + " " + longMessage))
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, mBuilder.build());
    }

    public static class LectureInfo {
        private Date start;
        private String title;
        private String archId;

        public LectureInfo(CalendarRow cr, LectureAppointmentsRow lar) {
            start = DateUtils.parseSqlDate(lar.getBeginn_datum_zeitpunkt() + ":00");
            title = cr.getTitle();
            archId = lar.getRaum_nr_architekt();
        }

        public Date getStart() {
            return start;
        }

        public String getTitle() {
            return title;
        }

        public String getArchId() {
            return archId;
        }
    }

    public static void updateWidget(Context c, SmartAlarmInfo info, boolean activating) {
        Utils.log("UPDATE WIDGET, activationg = " + activating + ", info = " + (info == null ? "null" : "not null"));
        Intent updateWidget = new Intent(c, SmartAlarmWidgetProvider.class);
        updateWidget.setAction(SmartAlarmWidgetProvider.ACTION_UPDATE_WIDGET);
        updateWidget.putExtra(SmartAlarmWidgetProvider.ACTIVATING, activating);
        updateWidget.putExtra(SmartAlarmReceiver.INFO, info);
        c.sendBroadcast(updateWidget);
    }
}
