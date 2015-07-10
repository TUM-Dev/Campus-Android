package de.tum.in.tumcampus.auxiliary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.Serializable;
import java.util.Date;

import de.tum.in.tumcampus.models.ConnectionToCampus;
import de.tum.in.tumcampus.models.LectureAppointmentsRow;
import de.tum.in.tumcampus.services.SmartAlarmReceiver;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;

public class AlarmSchedulerTask extends AsyncTask {
    private static final int PRE_ALARM_DIFF = 1;

    private Context c;

    private ConnectionToCampus ctc;

    private int requestCode;

    public AlarmSchedulerTask(Context c, int requestCode) {
        this(c, null, requestCode);
    }

    public AlarmSchedulerTask(Context c, ConnectionToCampus ctc, int requestCode) {
        this.c = c;
        this.requestCode = requestCode;

        if (ctc != null) {
            this.ctc = ctc;
        }
    }

    @Override
    protected Object doInBackground(Object[] params) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        boolean transportMode = prefs.getBoolean("smart_alarm_transportmode", true);
        Date lastAlarm = DateUtils.parseSqlDate(prefs.getString("smart_alarm_last", DateUtils.formatDateSql(new Date())));

        int buffer = Integer.parseInt(prefs.getString("smart_alarm_buffer", "10"));
        if (lastAlarm == null) {
            SmartAlarmUtils.showError(c, "SmartAlarm: internal error. Service deactivated.");
            return null;
        }

        if (!transportMode) {
            LectureAppointmentsRow lecture = SmartAlarmUtils.getFirstAppointment(c, lastAlarm);

            if (lecture == null) {
                SmartAlarmUtils.showError(c, "SmartAlarm: An error occured while fetching your lectures. Service deactivated.");
                return null;
            }

            // private transportation mode
            long estWakeUpTime = DateUtils.parseSqlDate(lecture.getBeginn_datum_zeitpunkt()).getTime()
                    - buffer * SmartAlarmUtils.MINUTEINMS
                    - prefs.getInt("smart_alarm_journeytime", 60) * SmartAlarmUtils.HOURINMS;
            schedule(estWakeUpTime, null);
        } else {
            String station_home = prefs.getString("smart_alarm_home", "");
            int minutesAtHome = Integer.parseInt(prefs.getString("smart_alarm_morningtime", "60"));
            if (station_home.equals("")) {
                SmartAlarmUtils.showError(c, "SmartAlarm: Home station is not set. Service deactivated.");
                return null;
            }

            LectureAppointmentsRow lecture = SmartAlarmUtils.getFirstAppointment(c, lastAlarm);

            if (lecture == null) {
                SmartAlarmUtils.showError(c, "SmartAlarm: An error occured while fetching your lectures. Service deactivated.");
                return null;
            }

            if (lecture.getRaum_nr_architekt() == null || lecture.getRaum_nr_architekt().equals("")) {
                SmartAlarmUtils.showError(c, "SmartAlarm: Location of your next lecture is unknown. Service deactivated.");
                return null;
            }

            TUMRoomFinderRequest roomFinder = new TUMRoomFinderRequest(c);
            String street = roomFinder.fetchRoomStreet(lecture.getRaum_nr_architekt());

            if (street == null) {
                SmartAlarmUtils.showError(c, "SmartAlarm: Location of your next lecture is not supported in public transport mode. Service deactivated.");
                return null;
            }

            long arrivalAtCampus = DateUtils.parseSqlDate(lecture.getBeginn_datum_zeitpunkt() + ":00").getTime()
                    - buffer * SmartAlarmUtils.MINUTEINMS;

            ConnectionToCampus toCampus;
            if (ctc != null) toCampus = SmartAlarmUtils.calculateJourney(c, ctc, arrivalAtCampus);
            else toCampus = SmartAlarmUtils.calculateJourney(c, station_home, street, arrivalAtCampus);

            long estWakeUpTime = toCampus.getDeparture() - minutesAtHome * SmartAlarmUtils.MINUTEINMS;
            schedule(estWakeUpTime, toCampus);
        }

        return null;
    }


    private void schedule(long estWakeUpTime, Serializable obj) {
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Service.ALARM_SERVICE);
        Intent i = new Intent(c, SmartAlarmReceiver.class);
        i.putExtra(SmartAlarmReceiver.PRE_ALARM, true);
        i.putExtra(SmartAlarmReceiver.EST_WAKEUP_TIME, estWakeUpTime);
        if (obj != null) i.putExtra(SmartAlarmReceiver.ROUTE, obj);
        PendingIntent p = PendingIntent.getBroadcast(c, requestCode, i, 0);

        // TODO: commented while in development
        //alarmManager.set(AlarmManager.RTC_WAKEUP, estWakeUpTime - PRE_ALARM_DIFF * SmartAlarmUtils.HOURINMS, p);
    }
}
