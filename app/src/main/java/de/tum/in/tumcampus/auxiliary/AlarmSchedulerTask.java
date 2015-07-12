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

import java.util.Date;

import de.tum.in.tumcampus.models.SmartAlarmInfo;
import de.tum.in.tumcampus.services.SmartAlarmReceiver;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;

public class AlarmSchedulerTask extends AsyncTask {
    private Context c;

    private SmartAlarmInfo sai;

    private int requestCode;

    private ProgressDialog pd;

    public AlarmSchedulerTask(Context c, ProgressDialog pd, int preAlarmRequest) {
        this (c, preAlarmRequest);
        this.pd = pd;
    }

    public AlarmSchedulerTask(Context c, int requestCode) {
        this(c, (SmartAlarmInfo) null, requestCode);
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
            return "SmartAlarm: internal error. Service deactivated.";
        }

        if (!publicTransport) {
            return schedulePrivateTransportationAlarm(prefs, lastAlarm, buffer);
        } else {
            return schedulePublicTransportationPreAlarm(prefs, lastAlarm, buffer);
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        if (pd != null) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
            editor.putBoolean("smart_alarm_activating", false);
            editor.apply();

            pd.dismiss();
        }

        if (o != null) {
            SmartAlarmUtils.showError(c, (String) o);
        }
    }

    private String schedulePublicTransportationPreAlarm(SharedPreferences prefs, Date lastAlarm, int buffer) {
        SmartAlarmInfo alarmInfo;
        int station_home = prefs.getInt("smart_alarm_home_id", -1);
        int minutesAtHome = Integer.parseInt(prefs.getString("smart_alarm_morningtime", "60"));
        if (station_home == -1) {
            return "Home station is not set. Service deactivated.";
        }

        SmartAlarmUtils.LectureInfo lecture = SmartAlarmUtils.getFirstAppointment(c, lastAlarm);

        if (lecture == null) {
            return "An error occurred while fetching your lectures. Service deactivated.";
        }

        if (lecture.getArchId() == null || lecture.getArchId().equals("")) {
            return "Location of your next lecture is unknown. Service deactivated.";
        }

        TUMRoomFinderRequest roomFinder = new TUMRoomFinderRequest(c);
        String street = roomFinder.fetchRoomStreet(lecture.getArchId());

        if (street == null) {
            return "Location of your next lecture is not supported in public transport mode. Service deactivated.";
        }

        long arrivalAtCampus = lecture.getStart().getTime()
                - buffer * SmartAlarmUtils.MINUTEINMS;

        if (sai != null) alarmInfo = SmartAlarmUtils.calculateJourney(c, sai, arrivalAtCampus);
        else alarmInfo = SmartAlarmUtils.calculateJourney(c, station_home, street, arrivalAtCampus);

        if (alarmInfo == null) {
            return "An error occurred while fetching route to campus. Service deactivated.";
        }

        alarmInfo.setWakeupTime(alarmInfo.getDeparture() - minutesAtHome * SmartAlarmUtils.MINUTEINMS);
        alarmInfo.setLectureTitle(lecture);
        schedule(alarmInfo);
        return null;
    }

    private String schedulePrivateTransportationAlarm(SharedPreferences prefs, Date lastAlarm, int buffer) {
        SmartAlarmInfo alarmInfo;SmartAlarmUtils.LectureInfo lecture = SmartAlarmUtils.getFirstAppointment(c, lastAlarm);

        if (lecture == null) {
            return "An error occurred while fetching your lectures. Service deactivated.";
        }

        // private transportation mode
        long estWakeUpTime = lecture.getStart().getTime()
                - buffer * SmartAlarmUtils.MINUTEINMS
                - Integer.parseInt(prefs.getString("smart_alarm_journeytime", "60")) * SmartAlarmUtils.MINUTEINMS;

        alarmInfo = new SmartAlarmInfo(estWakeUpTime, lecture);

        // pre alarm not needed
        requestCode = SmartAlarmReceiver.REQUEST_ALARM;
        schedule(alarmInfo);
        return null;
    }


    private void schedule(SmartAlarmInfo info) {
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Service.ALARM_SERVICE);
        Intent i = new Intent(c, SmartAlarmReceiver.class);
        i.putExtra(SmartAlarmReceiver.REQUEST_CODE, requestCode);
        i.putExtra(SmartAlarmReceiver.INFO, info);
        PendingIntent pi = PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        long diff = 0;
        if (requestCode == SmartAlarmReceiver.REQUEST_PRE_ALARM) diff = SmartAlarmReceiver.PRE_ALARM_DIFF * SmartAlarmUtils.HOURINMS;
        // TODO: commented while in development
        //alarmManager.set(AlarmManager.RTC_WAKEUP, estWakeUpTime - diff, p);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, pi);
    }
}
