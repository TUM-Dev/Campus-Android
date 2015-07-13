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
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.CalendarRow;
import de.tum.in.tumcampus.models.CalendarRowSet;
import de.tum.in.tumcampus.models.SmartAlarmInfo;
import de.tum.in.tumcampus.models.Geo;
import de.tum.in.tumcampus.models.LectureAppointmentsRow;
import de.tum.in.tumcampus.models.LectureAppointmentsRowSet;
import de.tum.in.tumcampus.services.SmartAlarmReceiver;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampus.widget.SmartAlarmWidgetProvider;

public class SmartAlarmUtils {
    public static final long MINUTEINMS = 60 * 1000;
    public static final long HOURINMS = 60 * MINUTEINMS;
    private static final int MONTH_BEFORE = 0;
    private static final int MONTH_AFTER = 3;

    public static void scheduleAlarm(Context c, ProgressDialog pd) {
        new AlarmSchedulerTask(c, pd).execute();
    }

    public static void cancelAlarm(Context c) {
        PendingIntent pi = PendingIntent.getBroadcast(c, 0, new Intent(c, SmartAlarmReceiver.class), 0);
        ((AlarmManager) c.getSystemService(Service.ALARM_SERVICE)).cancel(pi);
        pi.cancel();
    }

    public static void reSchedulePreAlarm(Context c, ProgressDialog pd) {
        cancelAlarm(c);
        scheduleAlarm(c, pd);
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

    public static LectureInfo getFirstAppointment(Context c, Date lastAlarm) {
        TUMOnlineRequest<CalendarRowSet> calendarRequest = new TUMOnlineRequest<>(TUMOnlineConst.CALENDER, c);
        calendarRequest.setParameter("pMonateVor", String.valueOf(MONTH_BEFORE));
        calendarRequest.setParameter("pMonateNach", String.valueOf(MONTH_AFTER));
        CalendarRowSet appointments = safeFetch(calendarRequest);

        if (appointments == null) {
            return null;
        }

        // calculate next day, where we want to set the alarm
        Calendar cal = new GregorianCalendar();
        if (DateUtils.isSameDay(lastAlarm, new Date())) {
            // midnight tomorrow
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        Date earliestNextLecture = cal.getTime();

        String lvnr = null;
        CalendarRow cr = null;
        // get first lecture after earliestNextLecture
        for (CalendarRow lecture : appointments.getKalendarList()) {
            Date d = DateUtils.parseSqlDate(lecture.getDtstart());
            // appointments sorted by date -> first appointment after calculated date is the first lecture on that day
            if (d != null && d.after(earliestNextLecture)) {
                // parse LvNr from url
                String url = lecture.getUrl();
                lvnr = url.substring(url.indexOf("cLvNr=")+6).split("&")[0];
                cr = lecture;
                break;
            }
        }

        if (lvnr == null) return null;

        TUMOnlineRequest<LectureAppointmentsRowSet> apts = new TUMOnlineRequest<>(TUMOnlineConst.LECTURES_APPOINTMENTS, c);
        apts.setParameter("pLVNr", lvnr);
        LectureAppointmentsRowSet lectureAppointments = safeFetch(apts);

        for (LectureAppointmentsRow appointment : lectureAppointments.getLehrveranstaltungenTermine()) {
            Date d = DateUtils.parseSqlDate(appointment.getBeginn_datum_zeitpunkt() + ":00");
            if (d != null && d.after(earliestNextLecture)) {
                // appointments sorted by date -> first appointment after calculated date is the first lecture on that day
                return new LectureInfo(cr, appointment);
            } else if (d == null) {
                Log.d("TCA", "couldn't parse " + appointment.getBeginn_datum_zeitpunkt() + " to date.");
            }
        }

        return null;
    }

    public static <T> T safeFetch(TUMOnlineRequest<T> tor) {
        try {
            return tor.fetch();
        } catch (Exception e) {
            Utils.log(e);
            return null;
        }
    }

    static void showError(Context c, String message) {
        // TODO: try to activate alarm for following day

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Smart Alarm")
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        ((NotificationManager) c.getSystemService(c.NOTIFICATION_SERVICE)).notify(0, mBuilder.build());

        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(c).edit();
        e.putBoolean("smart_alarm_active", false);
        e.apply();
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
        Intent updateWidget = new Intent();
        updateWidget.setAction(SmartAlarmWidgetProvider.ACTION_UPDATE_WIDGET);
        updateWidget.putExtra(SmartAlarmWidgetProvider.ACTIVATING, activating);
        updateWidget.putExtra(SmartAlarmReceiver.INFO, info);
        c.sendBroadcast(updateWidget);
    }
}
