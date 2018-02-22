package de.tum.in.tumcampusapp.utils;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;

public final class DateUtils {

    private static final long MINUTE_MILLIS = android.text.format.DateUtils.MINUTE_IN_MILLIS;
    private static final long HOUR_MILLIS = android.text.format.DateUtils.HOUR_IN_MILLIS;
    private static final long DAY_MILLIS = android.text.format.DateUtils.DAY_IN_MILLIS;

    private static final String FORMAT_SQL = "yyyy-MM-dd HH:mm:ss"; // 2014-06-30 16:31:57
    private static final String FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // 2014-06-30T16:31:57.878Z

    /*
     * Format an upcoming string nicely by being more precise as time comes closer
     */
    public static String getFutureTime(Date time, Context context) {
        if (time == null) {
            return "";
        }

        long timeInMillis = time.getTime();
        long now = Calendar.getInstance()
                           .getTimeInMillis();

        //Catch future dates: current clock might be running behind
        if (timeInMillis < now || timeInMillis <= 0) {
            return DateUtils.getTimeOrDay(time, context);
        }

        final long diff = timeInMillis - now;
        if (diff < 60 * MINUTE_MILLIS) {
            SimpleDateFormat formatter = new SimpleDateFormat("m", Locale.ENGLISH);
            return context.getString(R.string.IN) + ' ' + formatter.format(new Date(diff)) + ' ' + context.getString(R.string.MINUTES);
        } else if (diff < 3 * HOUR_MILLIS) { // Be more precise by telling the user the exact time if below 3 hours
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            return context.getString(R.string.AT) + ' ' + formatter.format(time);
        } else {
            return android.text.format.DateUtils.getRelativeTimeSpanString(timeInMillis, now, android.text.format.DateUtils.MINUTE_IN_MILLIS, android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE)
                                                .toString();
        }
    }

    /*
     * Format any given timestamp in a relative matter using the android methods
     */
    public static String getRelativeTimeISO(String timestamp, Context context) {
        return DateUtils.getRelativeTime(DateUtils.parseIsoDate(timestamp), context);
    }

    private static String getRelativeTime(Date date, Context context) {
        if (date == null) {
            return "";
        }

        return android.text.format.DateUtils.getRelativeDateTimeString(context, date.getTime(), MINUTE_MILLIS, DAY_MILLIS * 2L, 0)
                                            .toString();
    }

    /*
     * Format a past timestamp with degrading granularity
     */
    public static String getTimeOrDayISO(String datetime, Context context) {
        return DateUtils.getTimeOrDay(DateUtils.parseIsoDate(datetime), context);
    }

    public static String getTimeOrDay(String datetime, Context context) {
        return DateUtils.getTimeOrDay(DateUtils.parseSqlDate(datetime), context);
    }

    public static String getTimeOrDay(Date time, Context context) {
        if (time == null) {
            return "";
        }

        long timeInMillis = time.getTime();
        long now = Calendar.getInstance()
                           .getTimeInMillis();

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

    /*
     * Parsing string timestamps
     */
    public static Date parseSqlDate(String datetime) {
        if (datetime == null) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(DateUtils.FORMAT_SQL, Locale.ENGLISH); // 2014-06-30 16:31:57
            return formatter.parse(datetime);
        } catch (ParseException e) {
            Utils.log("Parsing SQL date failed");
        }
        return null;
    }

    public static Date parseIsoDate(String datetime) {
        if (datetime == null) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_ISO, Locale.ENGLISH);
            return formatter.parse(datetime);
        } catch (ParseException e) {
            Utils.log("Parsing SQL date failed");
        }
        return null;
    }

    /**
     * Checks whether two Dates contain the same day
     *
     * @return true if both dates are on the same day
     */
    public static boolean isSameDay(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
               && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Converts a date-string to Date
     *
     * @param str String with ISO-Date (yyyy-mm-dd)
     * @return Date
     */
    public static Date getDate(String str) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return dateFormat.parse(str);
        } catch (ParseException e) {
            Utils.log(e, str);
        }
        return new Date();
    }

    /**
     * Converts Date to an ISO date-string
     *
     * @param d Date
     * @return String (yyyy-mm-dd)
     */
    public static String getDateString(Date d) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return dateFormat.format(d);
    }

    /**
     * Converts Date to an ISO datetime-string
     *
     * @param d Date
     * @return String (yyyy-mm-dd hh:mm:ss)
     */
    public static String getDateTimeString(Date d) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return dateFormat.format(d);
    }

    /**
     * Converts a datetime-string to Date
     *
     * @param str String with ISO-DateTime (yyyy-mm-dd hh:mm:ss)
     * @return Date
     */
    public static Date getDateTime(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date = new Date();
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            Utils.log(e, str);
        }
        return date;
    }

    private DateUtils() {
    } // Utility class
}
