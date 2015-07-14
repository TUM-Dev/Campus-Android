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

import org.json.JSONException;

import java.io.IOException;
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
    public static final int RETRY_THRESHHOLD = 20;
    public static final int RETRY_INTERVAL = 8;
    public static final int RETRY_NOINTERNET_INTERVAL = 1;

    private Context c;

    private SmartAlarmInfo prevAlarmInfo;

    private ProgressDialog pd;

    /**
     * Indicates whether the calculations have been initiated by the user
     * (clicking on widget / in preferences screen) or initiated automatically
     */
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
        this.prevAlarmInfo = sai;

        userLaunched = false;
    }

    /**
     * Calculate route and schedule alarm
     * @param params not used
     * @return null if everything went fine, else an InsufficientDataException
     */
    @Override
    protected Object doInBackground(Object[] params) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        boolean publicTransport = prefs.getBoolean("smart_alarm_transportmode", true);
        Date lastAlarm = DateUtils.parseSqlDate(prefs.getString("smart_alarm_last", DateUtils.formatDateSql(new Date())));

        int buffer = Integer.parseInt(prefs.getString("smart_alarm_buffer", "10"));
        if (lastAlarm == null) {
            return new InsufficientDataException(c.getString(R.string.smart_alarm_internal_error), InsufficientDataException.NEVER);
        }

        if (!publicTransport) {
            return schedulePrivateTransportationAlarm(prefs, lastAlarm, buffer);
        } else {
            return schedulePublicTransportationPreAlarm(prefs, lastAlarm, buffer);
        }
    }

    /**
     * Show waiting on widget
     */
    @Override
    protected void onPreExecute() {
        // show waiting info on widget
        SmartAlarmUtils.updateWidget(c, null, true);
    }

    /**
     * Dismisses possible progress dialog and handles errors that occured during calculation
     * @param o InsufficientDataException or null
     */
    @Override
    protected void onPostExecute(Object o) {
        if (pd != null) {
            pd.dismiss();
        }

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(c).edit();
        prefs.putBoolean(Const.SMART_ALARM_ACTIVE, true);
        prefs.apply();

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
                        int hoursUntilLecture;
                        if (e.getLectureTime() == -1) hoursUntilLecture = RETRY_INTERVAL;
                        else hoursUntilLecture = (int) ((e.getLectureTime() - System.currentTimeMillis())/SmartAlarmUtils.HOURINMS + 1);

                        // lecture is too soon, show error and continue with the first one on the following day
                        SmartAlarmUtils.retryWithError(c, e.getMessage(), hoursUntilLecture);
                    } else {
                        // retry in RETRY_NOINTERNET_INTERVAL hours
                        SmartAlarmUtils.retryWithInfo(c, c.getString(R.string.smart_alarm_fetch_error));
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

    /**
     * Schedules an alarm in public transportation mode
     * @param prefs Default Shared Preferences
     * @param lastAlarm Date and time of the last alarm
     * @param buffer Time in minutes the user wants to arrive before the lecture starts
     * @return null if everything went fine, else an InsufficientDataException
     */
    private InsufficientDataException schedulePublicTransportationPreAlarm(SharedPreferences prefs, Date lastAlarm, int buffer) {
        SmartAlarmInfo alarmInfo;
        int station_home = prefs.getInt("smart_alarm_home_id", -1);
        int minutesAtHome = Integer.parseInt(prefs.getString("smart_alarm_morningtime", "60"));
        if (station_home == -1) {
            return new InsufficientDataException(c.getString(R.string.smart_alarm_no_home), InsufficientDataException.NEVER);
        }

        // if prevAlarmInfo != null, we already know next lecture, location etc
        if (prevAlarmInfo == null) {
            SmartAlarmUtils.LectureInfo lecture;
            String street = null;

            // get next lecture
            try {
                lecture = getFirstAppointment(c, lastAlarm);
            } catch (InsufficientDataException e) {
                return e;
            }

            if (lecture == null || lecture.getArchId() == null || lecture.getArchId().equals("")) {
                return new InsufficientDataException(c.getString(R.string.smart_alarm_unknown_location), InsufficientDataException.NEVER);
            }

            // get the street of the lecture room
            try {
                street = new TUMRoomFinderRequest(c).fetchRoomStreet(lecture.getArchId());
            } catch (IOException | JSONException e) {
                Utils.log(e.getMessage());
                Utils.log(e);
                return new InsufficientDataException(c.getString(R.string.smart_alarm_no_location), InsufficientDataException.SOON);
            }

            if (street == null) {
                return new InsufficientDataException(c.getString(R.string.smart_alarm_unsupported_location),
                        lecture.getStart().getTime(), InsufficientDataException.FOLLOWINGLECTURE);
            }

            // calculate route
            long arrivalAtCampus = lecture.getStart().getTime() - buffer * SmartAlarmUtils.MINUTEINMS;
            try {
                alarmInfo = SmartAlarmUtils.calculateJourney(c, station_home, street, arrivalAtCampus);
            } catch (InsufficientDataException e) {
                return new InsufficientDataException(e.getMessage(), lecture.getStart().getTime(), e.getRetryWhen());
            }

            if (alarmInfo == null) {
                return new InsufficientDataException(c.getString(R.string.smart_alarm_fetch_route_error), lecture.getStart().getTime(), InsufficientDataException.SOON);
            }

            // schedule alarm
            alarmInfo.setWakeupTime(alarmInfo.getDeparture() - minutesAtHome * SmartAlarmUtils.MINUTEINMS);
            alarmInfo.setLectureInfo(lecture);
            schedule(alarmInfo);
        } else {
            Utils.log("Make it easey....!");
            // update route info
            long arrivalAtCampus = prevAlarmInfo.getLectureStart().getTime() - buffer * SmartAlarmUtils.MINUTEINMS;
            try {
                alarmInfo = SmartAlarmUtils.calculateJourney(c, prevAlarmInfo, arrivalAtCampus);
            } catch (InsufficientDataException e) {
                return new InsufficientDataException(e.getMessage(), prevAlarmInfo.getLectureStart().getTime(), e.getRetryWhen());
            }

            // schedule alarm
            if (alarmInfo == null) schedule(prevAlarmInfo);
            else {
                alarmInfo.setWakeupTime(alarmInfo.getDeparture() - minutesAtHome * SmartAlarmUtils.MINUTEINMS);
                alarmInfo.setLectureInfo(new SmartAlarmUtils.LectureInfo(prevAlarmInfo.getLectureStart(), prevAlarmInfo.getLectureTitle(), prevAlarmInfo.getLectureRoom()));
                schedule(alarmInfo);
            }
        }

        return null;
    }

    /**
     * Schedules an alarm in private transportation mode
     * @param prefs Default Shared Preferences
     * @param lastAlarm Date and time of the last alarm
     * @param buffer Time in minutes the user wants to arrive before the lecture starts
     * @return null if everything went fine, else an InsufficientDataException
     */
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

    /**
     * Schedule an alarm
     * @param info All information about the alarm
     */
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

        // TODO: remove next two line after debug / showcase
        scheduleTime = System.currentTimeMillis() + 10000;
        if (prevAlarmInfo != null) action = SmartAlarmReceiver.ACTION_ALARM;

        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Service.ALARM_SERVICE);
        Intent i = new Intent(c, SmartAlarmReceiver.class);
        i.setAction(action);
        i.putExtra(SmartAlarmReceiver.INFO, info);
        PendingIntent pi = PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, scheduleTime, pi);

        // update widgets
        SmartAlarmUtils.updateWidget(c, info, false);

        Utils.log("SmartAlarm: scheduled " + action + " at "
                + android.text.format.DateUtils.formatDateTime(c, scheduleTime, android.text.format.DateUtils.FORMAT_SHOW_DATE) + " "
                + android.text.format.DateUtils.formatDateTime(c, scheduleTime, android.text.format.DateUtils.FORMAT_SHOW_TIME));

        Utils.log("Alarm goes off at "
                + android.text.format.DateUtils.formatDateTime(c, info.getWakeUpTime(), android.text.format.DateUtils.FORMAT_SHOW_DATE) + " "
                + android.text.format.DateUtils.formatDateTime(c, info.getWakeUpTime(), android.text.format.DateUtils.FORMAT_SHOW_TIME));
    }

    /**
     * Retrieve the first lecture today, tomorrow respectively the next scheduled appointment
     * @param c Context
     * @param lastAlarm Date and time of the last alarm
     * @return LectureInfo object containing information about the next scheduled lecture for which an alarm should be scheduled
     * @throws InsufficientDataException
     */
    public static SmartAlarmUtils.LectureInfo getFirstAppointment(Context c, Date lastAlarm) throws InsufficientDataException {
        TUMOnlineRequest<CalendarRowSet> calendarRequest = new TUMOnlineRequest<>(TUMOnlineConst.CALENDER, c);
        calendarRequest.setParameter("pMonateVor", String.valueOf(MONTH_BEFORE));
        calendarRequest.setParameter("pMonateNach", String.valueOf(MONTH_AFTER));
        CalendarRowSet appointments = SmartAlarmUtils.safeFetch(calendarRequest);

        if (appointments == null) throw new InsufficientDataException(c.getString(R.string.smart_alarm_no_calendar), InsufficientDataException.SOON);

        // whether to schedule alarm for today or not
        boolean forToday = true;

        // calculate next day, where we want to set the alarm
        Date earliestNextLecture = new GregorianCalendar().getTime();
        if (DateUtils.isSameDay(lastAlarm, new Date())) {
            // midnight tomorrow
            forToday = false;
            earliestNextLecture = getNextMidnight();
        }

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
                if (i == -1) throw new InsufficientDataException(c.getString(R.string.smart_alarm_missing_lvnr), d.getTime(), InsufficientDataException.FOLLOWINGLECTURE);
                lvnr = url.substring(url.indexOf("cLvNr=")+6).split("&")[0];
                cr = lecture;
                break;
            } else if (forToday && DateUtils.isSameDay(d, earliestNextLecture) && d.before(earliestNextLecture)) {
                // there has already been a lecture today => schedule for tomorrow
                forToday = false;
                earliestNextLecture = getNextMidnight();
            }
        }

        if (lvnr == null) {
            throw new InsufficientDataException(c.getString(R.string.smart_alarm_no_lectures), InsufficientDataException.SOON);
        }

        TUMOnlineRequest<LectureAppointmentsRowSet> apts = new TUMOnlineRequest<>(TUMOnlineConst.LECTURES_APPOINTMENTS, c);
        apts.setParameter("pLVNr", lvnr);
        LectureAppointmentsRowSet lectureAppointments = SmartAlarmUtils.safeFetch(apts);

        if (lectureAppointments == null) {
            throw new InsufficientDataException(c.getString(R.string.smart_alarm_fetch_lectures_error),
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

        throw new InsufficientDataException(c.getString(R.string.smart_alarm_no_lecture_appointment),
                DateUtils.parseSqlDate(cr.getDtstart()).getTime(), InsufficientDataException.FOLLOWINGLECTURE);
    }

    private static Date getNextMidnight() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public static class InsufficientDataException extends Exception {
        /**
         * Configuration error, disable alarm
         */
        public static final int NEVER = -1;

        /**
         * Temporary (e.g. connection) error, retry soon
         */
        public static final int SOON = 0;

        /**
         * Error with lecture appointment data / room data, retry on following day
         */
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
