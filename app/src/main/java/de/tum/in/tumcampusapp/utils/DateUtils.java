package de.tum.in.tumcampusapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

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
