package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import androidx.core.content.ContextCompat;

/**
 * Helper class for exporting to Google Calendar.
 */
public final class CalendarHelper {
    private static final String ACCOUNT_NAME = "TUM_Campus_APP";
    private static final String CALENDAR_NAME = "TUM Campus";

    /**
     * Gets uri query to insert calendar TUM_Campus_APP to google calendar
     *
     * @param c Context
     * @return Uri for insertion
     */
    public static Uri addCalendar(Context c) {
        final ContentValues cv = buildContentValues();
        return c.getContentResolver()
                .insert(buildCalUri(), cv);
    }

    /**
     * Deletes the calendar TUM_Campus_APP from google calendar
     *
     * @param c Context
     * @return Number of rows deleted
     */
    public static int deleteCalendar(Context c) {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        final Uri uri = CalendarContract.Calendars.CONTENT_URI;
        return c.getContentResolver()
                .delete(uri, " account_name = '" + ACCOUNT_NAME + '\'', null);
    }

    private static Uri buildCalUri() {
        return CalendarContract.Calendars.CONTENT_URI.buildUpon()
                                                     .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                                                     .appendQueryParameter(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                                                     .appendQueryParameter(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                                                     .build();
    }

    private static ContentValues buildContentValues() {
        final int colorCalendar = 0x0066CC;
        final String intName = ACCOUNT_NAME + CALENDAR_NAME;
        final ContentValues cv = new ContentValues();
        cv.put(Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
        cv.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        cv.put(Calendars.NAME, intName);
        cv.put(Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_NAME);
        cv.put(Calendars.CALENDAR_COLOR, colorCalendar);
        cv.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
        cv.put(Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
        cv.put(Calendars.VISIBLE, 1);
        cv.put(Calendars.SYNC_EVENTS, 1);
        return cv;
    }

    private CalendarHelper() {
        // CalendarHelper is a utility class
    }
}
