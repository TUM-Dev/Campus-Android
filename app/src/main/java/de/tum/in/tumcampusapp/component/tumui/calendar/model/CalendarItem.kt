package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import android.content.ContentValues
import android.graphics.Color
import android.provider.CalendarContract
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import java.util.regex.Pattern

enum class CalendarItemType {
    CANCELED, LECTURE, EXERCISE, OTHER
}

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

    val type: CalendarItemType
        get() {
            return if (isCanceled) {
                CalendarItemType.CANCELED
            } else if (title.endsWith("VO") || title.endsWith("VU")) {
                CalendarItemType.LECTURE
            } else if (title.endsWith("UE")) {
                CalendarItemType.EXERCISE
            } else {
                CalendarItemType.OTHER
            }
        }

    val isEditable: Boolean
        get() = url.isBlank()

    val isCanceled: Boolean
        get() = status == "CANCEL"

    val eventStart
        get() = dtstart

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
        return title == other.title &&
                dtstart == other.dtstart &&
                dtend == other.dtend
    }

    override fun toWeekViewEvent(): WeekViewEvent<CalendarItem> {
        val color = checkNotNull(color) { "No color provided for CalendarItem" }

        val backgroundColor = if (isCanceled) Color.WHITE else color
        val textColor = if (isCanceled) color else Color.WHITE
        val borderWidth = if (isCanceled) 2 else 0

        val style = WeekViewEvent.Style.Builder()
                .setBackgroundColor(backgroundColor)
                .setTextColor(textColor)
                .setTextStrikeThrough(isCanceled)
                .setBorderWidth(borderWidth)
                .setBorderColor(color)
                .build()

        return WeekViewEvent.Builder<CalendarItem>()
                .setId(nr.toLong())
                .setTitle(title)
                .setStartTime(eventStart.toGregorianCalendar())
                .setEndTime(eventEnd.toGregorianCalendar())
                .setLocation(location)
                .setStyle(style)
                .setAllDay(false)
                .setData(this)
                .build()
    }
}