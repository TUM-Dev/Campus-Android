package de.tum.in.tumcampus.auxiliary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.util.Date;

import de.tum.in.tumcampus.models.SmartAlarmInfo;
import de.tum.in.tumcampus.services.SmartAlarmReceiver;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;

public class AlarmSchedulerTask extends AsyncTask {
    private Context c;

    private SmartAlarmInfo sai;

    private int requestCode;

    public AlarmSchedulerTask(Context c, int requestCode) {
        this(c, null, requestCode);
    }

    public AlarmSchedulerTask(Context c, SmartAlarmInfo sai, int requestCode) {
        this.c = c;
        this.requestCode = requestCode;
        this.sai = sai;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        boolean publicTransport = prefs.getBoolean("smart_alarm_transportmode", true);
        Date lastAlarm = DateUtils.parseSqlDate(prefs.getString("smart_alarm_last", DateUtils.formatDateSql(new Date())));

        int buffer = Integer.parseInt(prefs.getString("smart_alarm_buffer", "10"));
        if (lastAlarm == null) {
            SmartAlarmUtils.showError(c, "SmartAlarm: internal error. Service deactivated.");
            return null;
        }

        if (!publicTransport) {
            schedulePrivateTransportationAlarm(prefs, lastAlarm, buffer);
        } else {
            schedulePublicTransportationPreAlarm(prefs, lastAlarm, buffer);
        }

        return null;
    }

    private void schedulePublicTransportationPreAlarm(SharedPreferences prefs, Date lastAlarm, int buffer) {
        SmartAlarmInfo alarmInfo;
        String station_home = prefs.getString("smart_alarm_home", "");
        int minutesAtHome = Integer.parseInt(prefs.getString("smart_alarm_morningtime", "60"));
        if (station_home.equals("")) {
            SmartAlarmUtils.showError(c, "SmartAlarm: Home station is not set. Service deactivated.");
            return;
        }

        SmartAlarmUtils.LectureInfo lecture = SmartAlarmUtils.getFirstAppointment(c, lastAlarm);

        if (lecture == null) {
            SmartAlarmUtils.showError(c, "SmartAlarm: An error occured while fetching your lectures. Service deactivated.");
            return;
        }

        if (lecture.getArchId() == null || lecture.getArchId().equals("")) {
            SmartAlarmUtils.showError(c, "SmartAlarm: Location of your next lecture is unknown. Service deactivated.");
            return;
        }

        TUMRoomFinderRequest roomFinder = new TUMRoomFinderRequest(c);
        String street = roomFinder.fetchRoomStreet(lecture.getArchId());

        if (street == null) {
            SmartAlarmUtils.showError(c, "SmartAlarm: Location of your next lecture is not supported in public transport mode. Service deactivated.");
            return;
        }

        long arrivalAtCampus = lecture.getStart().getTime()
                - buffer * SmartAlarmUtils.MINUTEINMS;

        if (sai != null) alarmInfo = SmartAlarmUtils.calculateJourney(c, sai, arrivalAtCampus);
        else alarmInfo = SmartAlarmUtils.calculateJourney(c, station_home, street, arrivalAtCampus);

        if (alarmInfo == null) {
            return;
        }

        alarmInfo.setWakeupTime(alarmInfo.getDeparture() - minutesAtHome * SmartAlarmUtils.MINUTEINMS);
        alarmInfo.setLectureTitle(lecture);
        schedule(alarmInfo);
    }

    private void schedulePrivateTransportationAlarm(SharedPreferences prefs, Date lastAlarm, int buffer) {
        SmartAlarmInfo alarmInfo;SmartAlarmUtils.LectureInfo lecture = SmartAlarmUtils.getFirstAppointment(c, lastAlarm);

        if (lecture == null) {
            SmartAlarmUtils.showError(c, "SmartAlarm: An error occured while fetching your lectures. Service deactivated.");
            return;
        }

        // private transportation mode
        long estWakeUpTime = lecture.getStart().getTime()
                - buffer * SmartAlarmUtils.MINUTEINMS
                - Integer.parseInt(prefs.getString("smart_alarm_journeytime", "60")) * SmartAlarmUtils.MINUTEINMS;

        alarmInfo = new SmartAlarmInfo(estWakeUpTime, lecture);

        // pre alarm not needed
        requestCode = SmartAlarmReceiver.ALARM_REQUEST;
        schedule(alarmInfo);
    }


    private void schedule(SmartAlarmInfo info) {
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Service.ALARM_SERVICE);
        Intent i = new Intent(c, SmartAlarmReceiver.class);
        i.putExtra(SmartAlarmReceiver.REQUEST_CODE, requestCode);
        i.putExtra(SmartAlarmReceiver.INFO, info);
        PendingIntent p = PendingIntent.getBroadcast(c, requestCode, i, 0);

        long diff = 0;
        if (requestCode == SmartAlarmReceiver.PRE_ALARM_REQUEST) diff = SmartAlarmReceiver.PRE_ALARM_DIFF * SmartAlarmUtils.HOURINMS;
        // TODO: commented while in development
        //alarmManager.set(AlarmManager.RTC_WAKEUP, estWakeUpTime - diff, p);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, p);
    }
}
