package de.tum.in.tumcampus.auxiliary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.CalendarRow;
import de.tum.in.tumcampus.models.CalendarRowSet;
import de.tum.in.tumcampus.models.LectureAppointmentsRow;
import de.tum.in.tumcampus.models.LectureAppointmentsRowSet;
import de.tum.in.tumcampus.models.SmartAlarmInfo;
import de.tum.in.tumcampus.services.SmartAlarmReceiver;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;

public class AlarmSchedulerTask extends AsyncTask {
    private static final int MONTH_BEFORE = 0;
    private static final int MONTH_AFTER = 3;
    private static final int RETRY_THRESHHOLD = 20;
    private static final int RETRY_INTERVAL = 8;
    private static final int RETRY_NOINTERNET_INTERVAL = 1;

    private Context c;

    private SmartAlarmInfo sai;

    private ProgressDialog pd;

    private boolean userLaunched;

    public AlarmSchedulerTask(Context c, ProgressDialog pd) {
        this (c);
        this.pd = pd;

        userLaunched = true;
    }

    public AlarmSchedulerTask(Context c, boolean userLaunched) {
        this (c);
        this.userLaunched = userLaunched;
    }

    public AlarmSchedulerTask(Context c) {
        this(c, (SmartAlarmInfo) null);
    }

    public AlarmSchedulerTask(Context c, SmartAlarmInfo sai) {
        this.c = c;
        this.sai = sai;

        userLaunched = false;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        boolean publicTransport = prefs.getBoolean("smart_alarm_transportmode", true);
        Date lastAlarm = DateUtils.parseSqlDate(prefs.getString("smart_alarm_last", DateUtils.formatDateSql(new Date())));

        int buffer = Integer.parseInt(prefs.getString("smart_alarm_buffer", "10"));
        if (lastAlarm == null) {
            return new InsufficientDataException(c.getString(R.string.SMART_ALARM_INTERNAL_ERROR), InsufficientDataException.NEVER);
        }

        if (!publicTransport) {
            return schedulePrivateTransportationAlarm(prefs, lastAlarm, buffer);
        } else {
            return schedulePublicTransportationPreAlarm(prefs, lastAlarm, buffer);
        }
    }

    @Override
    protected void onPreExecute() {
        // show waiting info on widget
        SmartAlarmUtils.updateWidget(c, null, true);
    }

    @Override
    protected void onPostExecute(Object o) {
        if (pd != null) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
            editor.putBoolean("smart_alarm_activating", false);
            editor.apply();

            pd.dismiss();
        }

        // only executed, when pre alarm calculations are performed
        if (o != null) {
            InsufficientDataException e = (InsufficientDataException) o;
            switch (e.getRetryWhen()) {
                // no lecture data or missing configurations, disable service
                case InsufficientDataException.NEVER:
                    SmartAlarmUtils.disableWithError(c, e.getMessage());
                    break;

                // data is missing due to connection error etc.
                case InsufficientDataException.SOON:
                    if (userLaunched) {
                        SmartAlarmUtils.retryWithError(c, e.getMessage(), RETRY_NOINTERNET_INTERVAL);
                        break;
                    }

                    if (e.isLectureTooSoon()) {
                        // lecture is too soon, show error and continue with the first one on the following day
                        int hoursUntilLecture = (int) ((e.getLectureTime() - System.currentTimeMillis())/SmartAlarmUtils.HOURINMS + 1);
                        SmartAlarmUtils.retryWithError(c, e.getMessage(), hoursUntilLecture);
                    } else {
                        // retry in RETRY_NOINTERNET_INTERVAL hours
                        SmartAlarmUtils.retryWithInfo(c, c.getString(R.string.SMART_ALARM_FETCH_ERROR), RETRY_NOINTERNET_INTERVAL);
                    }
                    break;

                // data is missing for next lecture, skip this one and continue with the first one on the following day
                case InsufficientDataException.FOLLOWINGLECTURE:
                    int hours;
                    // if the next lecture is within the next 24h
                    if (e.getLectureTime() != -1 && e.getLectureTime() - System.currentTimeMillis() < SmartAlarmUtils.DAYINMS) {
                        // retry 1h after lecture has started
                        hours = (int) ((e.getLectureTime() - System.currentTimeMillis())/SmartAlarmUtils.HOURINMS + 1);
                    } else {
                        hours = RETRY_INTERVAL;
                    }
                    SmartAlarmUtils.retryWithError(c, e.getMessage(), hours);
                    break;
            }
        }
    }

    private InsufficientDataException schedulePublicTransportationPreAlarm(SharedPreferences prefs, Date lastAlarm, int buffer) {
        SmartAlarmInfo alarmInfo;
        int station_home = prefs.getInt("smart_alarm_home_id", -1);
        int minutesAtHome = Integer.parseInt(prefs.getString("smart_alarm_morningtime", "60"));
        if (station_home == -1) {
            return new InsufficientDataException(c.getString(R.string.SMART_ALARM_NO_HOME), InsufficientDataException.NEVER);
        }

        SmartAlarmUtils.LectureInfo lecture = null;
        try {
            lecture = getFirstAppointment(c, lastAlarm);
        } catch (InsufficientDataException e) {
            // if we already have calculated a route in before, rely on this (outdated) route
            if (sai == null) return e;
        }

        if (lecture.getArchId() == null || lecture.getArchId().equals("") && sai == null) {
            return new InsufficientDataException(c.getString(R.string.SMART_ALARM_UNKNOWN_LOCATION), InsufficientDataException.NEVER);
        }

        TUMRoomFinderRequest roomFinder = new TUMRoomFinderRequest(c);
        String street = roomFinder.fetchRoomStreet(lecture.getArchId());

        if (street == null && sai == null) {
            return new InsufficientDataException(c.getString(R.string.SMART_ALARM_UNSUPPORTED_LOCATION),
                    lecture.getStart().getTime(), InsufficientDataException.FOLLOWINGLECTURE);
        }

        long arrivalAtCampus = lecture.getStart().getTime()
                - buffer * SmartAlarmUtils.MINUTEINMS;

        if (sai != null) alarmInfo = SmartAlarmUtils.calculateJourney(c, sai, arrivalAtCampus);
        else alarmInfo = SmartAlarmUtils.calculateJourney(c, station_home, street, arrivalAtCampus);

        if (alarmInfo == null && sai == null) {
            return new InsufficientDataException(c.getString(R.string.SMART_ALARM_FETCH_ROUTE_ERRORing_smart_alarm_fetch_route_error), lecture.getStart().getTime(), InsufficientDataException.SOON);
        }

        alarmInfo.setWakeupTime(alarmInfo.getDeparture() - minutesAtHome * SmartAlarmUtils.MINUTEINMS);
        alarmInfo.setLectureInfo(lecture);
        schedule(alarmInfo);
        return null;
    }

    private InsufficientDataException schedulePrivateTransportationAlarm(SharedPreferences prefs, Date lastAlarm, int buffer) {
        SmartAlarmUtils.LectureInfo lecture = null;
        try {
            lecture = getFirstAppointment(c, lastAlarm);
        } catch (InsufficientDataException e) {
            return e;
        }

        long estWakeUpTime = lecture.getStart().getTime()
                - buffer * SmartAlarmUtils.MINUTEINMS
                - Integer.parseInt(prefs.getString("smart_alarm_journeytime", "60")) * SmartAlarmUtils.MINUTEINMS;

        schedule(new SmartAlarmInfo(estWakeUpTime, lecture));
        return null;
    }

    private void schedule(SmartAlarmInfo info) {
        long diff = SmartAlarmReceiver.PRE_ALARM_DIFF * SmartAlarmUtils.HOURINMS;
        long scheduleTime = info.getWakeUpTime();

        // if private transportation or pre alarm is in the past, directly schedule alarm
        String action = SmartAlarmReceiver.ACTION_PREALARM;
        if (info.getWakeUpTime() - diff - 5000 < new Date().getTime() || info.getFirstTransportType() == SmartAlarmInfo.TransportType.PRIVATE) {
            action = SmartAlarmReceiver.ACTION_ALARM;
        } else {
            scheduleTime-= diff;
        }

        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Service.ALARM_SERVICE);
        Intent i = new Intent(c, SmartAlarmReceiver.class);
        i.setAction(action);
        i.putExtra(SmartAlarmReceiver.INFO, info);
        PendingIntent pi = PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        // TODO: comment at deployment
        scheduleTime = System.currentTimeMillis() + 3000;
        alarmManager.set(AlarmManager.RTC_WAKEUP, scheduleTime, pi);

        // update widgets
        SmartAlarmUtils.updateWidget(c, info, false);
    }


    public static SmartAlarmUtils.LectureInfo getFirstAppointment(Context c, Date lastAlarm) throws InsufficientDataException {
        TUMOnlineRequest<CalendarRowSet> calendarRequest = new TUMOnlineRequest<>(TUMOnlineConst.CALENDER, c);
        calendarRequest.setParameter("pMonateVor", String.valueOf(MONTH_BEFORE));
        calendarRequest.setParameter("pMonateNach", String.valueOf(MONTH_AFTER));
        CalendarRowSet appointments = SmartAlarmUtils.safeFetch(calendarRequest);

        if (appointments == null) throw new InsufficientDataException(c.getString(R.string.SMART_ALARM_NO_CALENDAR), InsufficientDataException.SOON);

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
                int i = url.indexOf("cLvNr=");
                if (i == -1) throw new InsufficientDataException(c.getString(R.string.SMART_ALARM_MISSING_LVNR), d.getTime(), InsufficientDataException.FOLLOWINGLECTURE);
                lvnr = url.substring(url.indexOf("cLvNr=")+6).split("&")[0];
                cr = lecture;
                break;
            }
        }

        if (lvnr == null) {
            throw new InsufficientDataException(c.getString(R.string.SMART_ALARM_NO_LECTURES), InsufficientDataException.SOON);
        }

        TUMOnlineRequest<LectureAppointmentsRowSet> apts = new TUMOnlineRequest<>(TUMOnlineConst.LECTURES_APPOINTMENTS, c);
        apts.setParameter("pLVNr", lvnr);
        LectureAppointmentsRowSet lectureAppointments = SmartAlarmUtils.safeFetch(apts);

        if (lectureAppointments == null) {
            throw new InsufficientDataException(c.getString(R.string.SMART_ALARM_FETCH_LECTURES_ERROR),
                    DateUtils.parseSqlDate(cr.getDtstart()).getTime(), InsufficientDataException.SOON);
        }

        for (LectureAppointmentsRow appointment : lectureAppointments.getLehrveranstaltungenTermine()) {
            Date d = DateUtils.parseSqlDate(appointment.getBeginn_datum_zeitpunkt() + ":00");
            if (d != null && d.after(earliestNextLecture)) {
                // appointments sorted by date -> first appointment after calculated date is the first lecture on that day
                return new SmartAlarmUtils.LectureInfo(cr, appointment);
            } else if (d == null) {
                Log.d("TCA", "couldn't parse " + appointment.getBeginn_datum_zeitpunkt() + " to date.");
            }
        }

        throw new InsufficientDataException(c.getString(R.string.SMART_ALARM_NO_LECTURE_APPOINTMENT),
                DateUtils.parseSqlDate(cr.getDtstart()).getTime(), InsufficientDataException.FOLLOWINGLECTURE);
    }

    private static class InsufficientDataException extends Exception {
        public static final int NEVER = -1;
        public static final int SOON = 0;
        public static final int FOLLOWINGLECTURE = 1;

        private int retryWhen;

        private long lectureTime = -1;

        public InsufficientDataException(String message, long time, int retryWhen) {
            this(message, retryWhen);
            lectureTime = time;
        }

        public InsufficientDataException(String message, int retryWhen) {
            super(message);

            this.retryWhen = retryWhen;
        }

        public int getRetryWhen() {
            return retryWhen;
        }

        public long getLectureTime() {
            return lectureTime;
        }

        public boolean isLectureTooSoon() {
            return (lectureTime == -1 || lectureTime - System.currentTimeMillis() < SmartAlarmUtils.DAYINMS)
                    && new GregorianCalendar().get(Calendar.HOUR_OF_DAY) > RETRY_THRESHHOLD;
        }
    }
}
