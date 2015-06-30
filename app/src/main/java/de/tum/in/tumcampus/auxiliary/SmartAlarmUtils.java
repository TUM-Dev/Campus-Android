package de.tum.in.tumcampus.auxiliary;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.tum.in.tumcampus.models.CalendarRow;
import de.tum.in.tumcampus.models.CalendarRowSet;
import de.tum.in.tumcampus.models.LectureAppointmentsRow;
import de.tum.in.tumcampus.models.LectureAppointmentsRowSet;
import de.tum.in.tumcampus.services.SmartAlarmReceiver;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequestFetchListener;

public class SmartAlarmUtils {
    private static final long HOURINMS = 60 * 60 * 1000;
    private static final long DAYINMS = HOURINMS * 24;

    public static long getCalculationTime(String home, String campus, long arrivalAtCampus, int timeAtHome) {
        return 0;
    }

    public static long getWakeUpTime(String home, String campus, long arrivalAtCampus, int timeAtHome) {
        return 0;
    }

    public static void schedulePreAlarm(final Context c) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String station_home = prefs.getString("smart_alarm_home", "");
        
        // TODO: obtain campus of next lecture
        String station_campus = "";
        int minutesAtHome = Integer.parseInt(prefs.getString("smart_alarm_morningtime", "60"));

        // TODO: calculate time to be on campus
        long arrivalAtCampus = 0;
        TUMOnlineRequest<CalendarRowSet> calendarRequest = new TUMOnlineRequest<>(TUMOnlineConst.CALENDER, c);
        calendarRequest.fetchInteractive(c, new TUMOnlineRequestFetchListener<CalendarRowSet>() {
            @Override
            public void onNoInternetError() {
                Toast.makeText(c, "No internet connection & no cached lectures. Smart Alarm deactivated.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFetch(CalendarRowSet response) {
                List<CalendarRow> appointments = response.getKalendarList();

                // calculate next day, where we want to set the alarm
                Date lastAlarm = DateUtils.parseSqlDate(prefs.getString("smart_alarm_last", ""));
                Calendar cal = new GregorianCalendar();
                if (DateUtils.isSameDay(lastAlarm, new Date())) {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
                final Date now = cal.getTime();

                for (CalendarRow l : appointments) {
                    Date d = DateUtils.parseSqlDate(l.getDtstart());
                    if (d != null && d.after(now)) {
                        Log.d("TCA", l.getTitle() + " um " + d + " im raum " + l.getLocation());

                        // parse LvNr from url
                        String url = l.getUrl();
                        url = url.substring(url.indexOf("cLvNr=")+6);
                        String lvnr = url.split("&")[0];
                        TUMOnlineRequest<LectureAppointmentsRowSet> apts = new TUMOnlineRequest<>(TUMOnlineConst.LECTURES_APPOINTMENTS, c);
                        apts.setParameter("pLVNr", lvnr);
                        apts.fetchInteractive(c, new TUMOnlineRequestFetchListener<LectureAppointmentsRowSet>() {
                            @Override
                            public void onNoInternetError() {
                                Toast.makeText(c, "No internet connection & no cached lectures. Smart Alarm deactivated.", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFetch(LectureAppointmentsRowSet response) {
                                List<LectureAppointmentsRow> appointments = response.getLehrveranstaltungenTermine();

                                for (LectureAppointmentsRow a : appointments) {
                                    Date d = DateUtils.parseSqlDate(a.getBeginn_datum_zeitpunkt() + ":00");
                                    if (d != null && d.after(now)) {
                                        // appointments sorted by date -> first appointment after calculated date is the first lecture on that day
                                        Log.d("TCA", "first meeting in room " + a.getOrt() + " / " + a.getRaum_nr() + " / " + a.getRaum_nr_architekt());
                                        Log.d("TCA", "appointment: " + a.getTermin_betreff());
                                        break;
                                    } else if (d == null) {
                                        Log.d("TCA", "couldn't parse " + a.getBeginn_datum_zeitpunkt());
                                    }
                                }
                            }

                            @Override
                            public void onFetchCancelled() {
                                // TODO: cancel request and stuff
                            }

                            @Override
                            public void onFetchError(String errorReason) {
                                Toast.makeText(c, "Error fetching lectures. Smart Alarm deactivated.", Toast.LENGTH_LONG).show();
                            }
                        });

                        break;
                    }
                }
            }

            @Override
            public void onFetchCancelled() {
                // TODO: cancel request and stuff
            }

            @Override
            public void onFetchError(String errorReason) {
                Toast.makeText(c, "Error fetching lectures. Smart Alarm deactivated.", Toast.LENGTH_LONG).show();
            }
        });

        long estWakeUpTime = calculateJourneyTime(station_home, station_campus, minutesAtHome, arrivalAtCampus);

        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Service.ALARM_SERVICE);
        Intent i = new Intent(c, SmartAlarmReceiver.class);
        i.putExtra(SmartAlarmReceiver.PRE_ALARM, true);
        PendingIntent p = PendingIntent.getBroadcast(c, SmartAlarmReceiver.PRE_ALARM_REQUEST, i, 0);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, estWakeUpTime - HOURINMS, p);
    }

    private static long calculateJourneyTime(String station_home, String station_campus, int minutesAtHome, long arrivalAtCampus) {
        return 0;
    }
}
