package de.tum.`in`.tumcampusapp.component.tumui.calendar

import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderSchedule
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime

/**
 * A class to represent events for the integrated WeekView calendar
 */
data class WidgetCalendarItem(
        val id: String,
        val title: String,
        val startTime: DateTime,
        val endTime: DateTime,
        val location: String
) : WeekViewDisplayable<WidgetCalendarItem> {

    var color: Int = 0

    var isFirstOnDay: Boolean = false

    override fun toWeekViewEvent(): WeekViewEvent<WidgetCalendarItem> {
        val style = WeekViewEvent.Style.Builder()
                .setBackgroundColor(color)
                .build()

        return WeekViewEvent.Builder<WidgetCalendarItem>()
                .setId(id.toLong())
                .setTitle(title)
                .setStartTime(startTime.toGregorianCalendar())
                .setEndTime(endTime.toGregorianCalendar())
                .setLocation(location)
                .setAllDay(false)
                .setStyle(style)
                .setData(this)
                .build()
    }

    companion object {

        @JvmStatic
        fun create(calendarItem: CalendarItem): WidgetCalendarItem {
            return WidgetCalendarItem(
                    calendarItem.nr,
                    calendarItem.getFormattedTitle(),
                    calendarItem.eventStart,
                    calendarItem.eventEnd,
                    calendarItem.getEventLocation()
            )
        }

        @JvmStatic
        fun create(schedule: RoomFinderSchedule): WidgetCalendarItem {
            val id = java.lang.Long.toString(schedule.event_id)
            val start = DateTimeUtils.getDateTime(schedule.start)
            val end = DateTimeUtils.getDateTime(schedule.end)
            return WidgetCalendarItem(id, schedule.title, start, end, "")
        }

    }

}
