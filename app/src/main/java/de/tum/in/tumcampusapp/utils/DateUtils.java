package de.tum.in.tumcampusapp.utils;

import java.util.Calendar;

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

    private DateUtils() {
    } // Utility class
}
