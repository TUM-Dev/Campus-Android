package de.tum.`in`.tumcampusapp.models.tumo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.content.ContentValues
import android.provider.CalendarContract
import de.tum.`in`.tumcampusapp.auxiliary.Utils
import de.tum.`in`.tumcampusapp.auxiliary.calendar.IntegratedCalendarEvent
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Entity for storing information about lecture events
 */
@Entity(tableName="calendar")
data class CalendarItem(@PrimaryKey
                        var nr: String = "",
                        var status: String = "",
                        var url: String = "",
                        var title: String = "",
                        var description: String = "",
                        var dtstart: String = "",
                        var dtend: String = "",
                        var location: String = "",
                        @Ignore
                        var blacklisted: Boolean = false) {
    /**
     * Returns the color of the event
     */
    fun getEventColor(): Int {
        return if (title.endsWith("VO") || title.endsWith("VU")) {
            IntegratedCalendarEvent.getDisplayColorFromColor(-0xd76de1)
        } else if (title.endsWith("UE")) {
            IntegratedCalendarEvent.getDisplayColorFromColor(-0x8000)
        } else {
            IntegratedCalendarEvent.getDisplayColorFromColor(-0xffff01)
        }
    }

    /**
     * Get event start as Calendar object
     */
    fun getEventStart(): Calendar {
        val result = Calendar.getInstance()
        result.time = Utils.getDateTime(dtstart)
        return result
    }

    /**
     * Get event end as Calendar object
     */
    fun getEventEnd(): Calendar {
        val result = Calendar.getInstance()
        result.time = Utils.getDateTime(dtend)
        return result
    }

    /**
     * Formats title to exclude codes
     */
    fun getFormattedTitle(): String {
        return Pattern.compile("\\([A-Z0-9\\.]+\\)")
                .matcher(Pattern.compile("\\([A-Z]+[0-9]+\\)")
                        .matcher(Pattern.compile("[A-Z, 0-9(LV\\.Nr)=]+$")
                                .matcher(title)
                                .replaceAll(""))
                        .replaceAll(""))
                .replaceAll("")!!
                .trim { it <= ' ' }
    }

    /**
     * Formats event's location
     */
    fun getEventLocation(): String {
        return Pattern.compile("\\([A-Z0-9\\.]+\\)")
                .matcher(location)
                .replaceAll("")!!
                .trim { it <= ' ' }
    }


    fun getEventDateString(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val startDate = Utils.getDateTime(dtstart)
        val endDate = Utils.getDateTime(dtend)
        return String.format("%s %s - %s", dateFormat.format(startDate), timeFormat.format(startDate), timeFormat.format(endDate))
    }


    /**
     * Prepares ContentValues object with related values plugged
     */
    fun toContentValues(): ContentValues {
        val values = ContentValues()

        // Put the received values into a contentResolver to
        // transmit the to Google Calendar
        values.put(CalendarContract.Events.DTSTART, Utils.getDateTime(dtstart).time)
        values.put(CalendarContract.Events.DTEND, Utils.getDateTime(dtend).time)
        values.put(CalendarContract.Events.TITLE, title)
        values.put(CalendarContract.Events.DESCRIPTION, description)
        values.put(CalendarContract.Events.EVENT_LOCATION, location)
        return values
    }
}