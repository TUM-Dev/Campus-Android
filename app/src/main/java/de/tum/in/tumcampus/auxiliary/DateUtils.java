package de.tum.in.tumcampus.auxiliary;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.tum.in.tumcampus.R;

public class DateUtils {

    private static final long MINUTE_MILLIS = android.text.format.DateUtils.MINUTE_IN_MILLIS;
    private static final long HOUR_MILLIS = android.text.format.DateUtils.HOUR_IN_MILLIS;
    private static final long DAY_MILLIS = android.text.format.DateUtils.DAY_IN_MILLIS;

    private static final String formatSQL = "yyyy-MM-dd HH:mm:ss"; // 2014-06-30 16:31:57
    private static final String formatISO = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // 2014-06-30T16:31:57.878Z

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
        long now = Calendar.getInstance().getTimeInMillis();

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

    public static Date parseSqlDate(String datetime) {
        if(datetime == null) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(DateUtils.formatSQL, Locale.ENGLISH); // 2014-06-30 16:31:57
            return formatter.parse(datetime);
        } catch (ParseException e) {
            Utils.log("Parsing SQL date failed");
        }
        return null;
    }

    public static Date parseIsoDate(String datetime) {
        if(datetime == null) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(formatISO, Locale.ENGLISH);
            return formatter.parse(datetime);
        } catch (ParseException e) {
            Utils.log("Parsing SQL date failed");
        }
        return null;
    }
}
