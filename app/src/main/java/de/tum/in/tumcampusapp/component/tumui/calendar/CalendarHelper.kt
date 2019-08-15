package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R

/**
 * Helper class for exporting to Google Calendar.
 */
object CalendarHelper {
    private const val ACCOUNT_NAME = "TUM_Campus_APP"
    private const val CALENDAR_NAME = "TUM Campus"

    /**
     * Gets uri query to insert calendar TUM_Campus_APP to google calendar
     *
     * @param context Context
     * @return Uri for insertion
     */
    fun addCalendar(context: Context): Uri? = context.contentResolver.insert(buildCalendarUri(), buildContentValues(context))

    /**
     * Deletes the calendar TUM_Campus_APP from google calendar
     *
     * @param c Context
     * @return Number of rows deleted
     */
    fun deleteCalendar(c: Context): Int {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return 0
        }
        val uri = Calendars.CONTENT_URI
        return c.contentResolver
                .delete(uri, " account_name = '$ACCOUNT_NAME'", null)
    }

    private fun buildCalendarUri(): Uri {
        return CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build()
    }

    private fun buildContentValues(c: Context): ContentValues {
        val calendarColor = c.resources.getColor(R.color.calendar_color)
        val intName = ACCOUNT_NAME + CALENDAR_NAME
        return ContentValues().apply {
            put(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(Calendars.NAME, intName)
            put(Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_NAME)
            put(Calendars.CALENDAR_COLOR, calendarColor)
            put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER)
            put(Calendars.OWNER_ACCOUNT, ACCOUNT_NAME)
            put(Calendars.VISIBLE, 1)
            put(Calendars.SYNC_EVENTS, 1)
        }
    }
}
