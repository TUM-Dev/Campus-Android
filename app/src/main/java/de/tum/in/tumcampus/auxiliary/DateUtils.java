package de.tum.in.tumcampus.auxiliary;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.GregorianCalendar;

import de.tum.in.tumcampus.R;

public class DateUtils {

    private static final long MINUTE_MILLIS = android.text.format.DateUtils.MINUTE_IN_MILLIS;
    private static final long HOUR_MILLIS = android.text.format.DateUtils.HOUR_IN_MILLIS;
    private static final long DAY_MILLIS = android.text.format.DateUtils.DAY_IN_MILLIS;

    private static final String formatSQL = "yyyy-MM-dd HH:mm:ss"; // 2014-06-30 16:31:57
    private static final String formatISO = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // 2014-06-30T16:31:57.878Z
    private static final String SIMPLE_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";
    private static final String logTag = "DateUtils";

    public static String getRelativeTimeISO(String timestamp, Context context) {
        return DateUtils.getRelativeTime(DateUtils.parseIsoDate(timestamp), context);
    }

    private static String getRelativeTime(Date date, Context context) {
        if (date == null) {
            return "";
        }

        return android.text.format.DateUtils.getRelativeDateTimeString(context, date.getTime(),
                MINUTE_MILLIS, DAY_MILLIS * 2L, 0).toString();
    }

    public static String getTimeOrDay(String datetime, Context context) {
        return DateUtils.getTimeOrDay(DateUtils.parseSqlDate(datetime), context);
    }

    public static String getTimeOrDay(Date time, Context context) {
        if (time == null) {
            return "";
        }

        long timeInMillis = time.getTime();
        long now = DateUtils.getCurrentTime().toMillis(false);

        //Catch future dates: current clock might be running behind
        if (timeInMillis > now || timeInMillis <= 0) {
            return context.getString(R.string.just_now);
        }

        final long diff = now - timeInMillis;
        if (diff < MINUTE_MILLIS) {
            return context.getString(R.string.just_now);
        } else if (diff < 24 * HOUR_MILLIS) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            return formatter.format(time);
        } else if (diff < 48 * HOUR_MILLIS) {
            return context.getString(R.string.yesterday);
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
            return formatter.format(time);
        }
    }

    public static String formatDateSql(Date d) {
        if (d == null) {
            return null;
        }

        return new SimpleDateFormat(DateUtils.formatSQL, Locale.ENGLISH).format(d);
    }

    public static Date parseSqlDate(String datetime) {
        if (datetime == null) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(DateUtils.formatSQL, Locale.ENGLISH); // 2014-06-30 16:31:57
            return formatter.parse(datetime);
        } catch (ParseException e) {
            Log.e(logTag, "Parsing SQL date failed");
        }
        return null;
    }

    public static Date parseIsoDate(String datetime) {
        if (datetime == null) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(formatISO, Locale.ENGLISH);
            return formatter.parse(datetime);
        } catch (ParseException e) {
            Log.e(logTag, "Parsing SQL date failed");
        }
        return null;
    }


    public static Date parseSimpleDateFormat(String datetime) {
        if(datetime == null) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH);
            return formatter.parse(datetime);
        } catch (ParseException e) {
            Log.e(logTag, "Parsing SIMPLE_DATE_FORMAT date failed");
        }
        return null;
    }

    private static Time getCurrentTime() {
        Time now = new Time();
        now.setToNow();
        return now;
    }

    public static boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return false;
        }
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);

        return c1.get(Calendar.ERA) == c2.get(Calendar.ERA)
                && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isToday(Date d) {
        return isSameDay(d, new Date());
    }

    public static GregorianCalendar epochToDate(Long epochTime){
        /**
         * gets the epochTime in seconds
         * returns the corresponding GeorgianCalendar
         */
        if(epochTime == null) {
            return null;
        }
        GregorianCalendar c = new GregorianCalendar();
        //  This method gets the epochTime in milliseconds
        c.setTimeInMillis(epochTime * 1000);
        return c;
    }
}
