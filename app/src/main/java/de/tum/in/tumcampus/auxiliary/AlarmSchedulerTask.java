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
import de.tum.in.tumcampus.widget.SmartAlarmWidgetProvider;

public class AlarmSchedulerTask extends AsyncTask {
    private Context c;

    private SmartAlarmInfo sai;

    private ProgressDialog pd;

    public AlarmSchedulerTask(Context c, ProgressDialog pd) {
        this (c);
        this.pd = pd;
    }

    public AlarmSchedulerTask(Context c) {
        this(c, (SmartAlarmInfo) null);
    }

    public AlarmSchedulerTask(Context c, SmartAlarmInfo sai) {
        this.c = c;
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
        alarmInfo.setLectureInfo(lecture);
        schedule(alarmInfo);
        return null;
    }

    private String schedulePrivateTransportationAlarm(SharedPreferences prefs, Date lastAlarm, int buffer) {
        SmartAlarmUtils.LectureInfo lecture = SmartAlarmUtils.getFirstAppointment(c, lastAlarm);

        if (lecture == null) {
            return "An error occurred while fetching your lectures. Service deactivated.";
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
        if (info.getWakeUpTime() - diff > new Date().getTime() - 1000 || info.getFirstTransportType() == SmartAlarmInfo.TransportType.PRIVATE) {
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
}
