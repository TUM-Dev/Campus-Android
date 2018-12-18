package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.WidgetCalendarItem
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Entity for storing information about lecture events
 */
@Entity(tableName = "calendar")
data class CalendarItem(
        @PrimaryKey
        var nr: String = "",
        var status: String = "",
        var url: String = "",
        var title: String = "",
        var description: String = "",
        var dtstart: DateTime = DateTime(),
        var dtend: DateTime = DateTime(),
        var location: String = "",
        @Ignore
        var blacklisted: Boolean = false
) : WeekViewDisplayable<CalendarItem> {

    @Ignore
    var color: Int? = null

    val isEditable: Boolean
        get() = url.isBlank()

    /**
     * Returns the color of the event
     */
    // TODO: Move into EventsColorProvider
    fun getEventColor(context: Context): Int {
        return if (isCancelled()) {
            WidgetCalendarItem.getDisplayColorFromColor(ContextCompat.getColor(context, R.color.event_canceled))
        } else if (title.endsWith("VO") || title.endsWith("VU")) {
            WidgetCalendarItem.getDisplayColorFromColor(ContextCompat.getColor(context, R.color.event_lecture))
        } else if (title.endsWith("UE")) {
            WidgetCalendarItem.getDisplayColorFromColor(ContextCompat.getColor(context, R.color.event_exercise))
        } else {
            WidgetCalendarItem.getDisplayColorFromColor(ContextCompat.getColor(context, R.color.event_other))
        }
    }

    /**
     * Get event start as Calendar object
     */
    val eventStart
        get() = dtstart

    /**
     * Get event end as Calendar object
     */
    val eventEnd
        get() = dtend

    /**
     * Formats title to exclude codes
     */
    fun getFormattedTitle(): String {
        // remove lecture codes in round or square brackets e.g. (IN0003), [MA0902]
        return Pattern.compile("[(\\[][A-Z0-9.]+[)\\]]")
                // remove type of lecture (VO, UE, SE, PR) at the end of the line
                .matcher(Pattern.compile(" (UE|VO|SE|PR)$")
                        .matcher(title)
                        .replaceAll(""))
                .replaceAll("")
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
        val timeFormat = DateTimeFormat.forPattern("HH:mm").withLocale(Locale.US)
        val dateFormat = DateTimeFormat.forPattern("EEE, dd.MM.yyyy").withLocale(Locale.US)
        return String.format("%s %s - %s", dateFormat.print(eventStart), timeFormat.print(eventStart), timeFormat.print(eventEnd))
    }

    /**
     * Prepares ContentValues object with related values plugged
     */
    fun toContentValues(): ContentValues {
        val values = ContentValues()

        // Put the received values into a contentResolver to
        // transmit the to Google Calendar
        values.put(CalendarContract.Events.DTSTART, eventStart.millis)
        values.put(CalendarContract.Events.DTEND, eventEnd.millis)
        values.put(CalendarContract.Events.TITLE, title)
        values.put(CalendarContract.Events.DESCRIPTION, description)
        values.put(CalendarContract.Events.EVENT_LOCATION, location)
        return values
    }

    fun isSameEventButForLocation(other: CalendarItem): Boolean {
        return title.equals(other.title)
                && dtstart.equals(other.dtstart)
                && dtend.equals(other.dtend)
    }

    fun isCancelled(): Boolean = status == "CANCEL"

    override fun toWeekViewEvent(): WeekViewEvent<CalendarItem> {
        return WeekViewEvent(nr.toLong(), title, eventStart.toGregorianCalendar(),
                eventEnd.toGregorianCalendar(), location, 0, false, this)
    }
}