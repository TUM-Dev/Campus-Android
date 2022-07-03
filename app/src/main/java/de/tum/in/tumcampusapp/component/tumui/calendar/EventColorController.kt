package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.content.Context
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItemType
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.EventColor
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.ColorUtils.getDisplayColorFromColor
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class EventColorController(
    private val context: Context
) {
    private val eventColorDao: EventColorDao = TcaDb.getInstance(context).classColorDao()

    fun changeEventColor(calendarItem: CalendarItem, color: Int, isSingleEvent: Boolean = false) {
        if (isSingleEvent) {
            val list = eventColorDao.getByEventNrAndIdentifierAndIsSingleEvent(calendarItem.nr, getEventIdentifier(calendarItem), true)
            insertEventColor(list, calendarItem, true, color)
        } else {
            val list = eventColorDao.getByIdentifierAndIsSingleEvent(getEventIdentifier(calendarItem), false)
            insertEventColor(list, calendarItem, false, color)
        }
    }

    private fun insertEventColor(list: List<EventColor>, calendarItem: CalendarItem, isSingleEvent: Boolean, color: Int) {
        if (list.isEmpty()) {
            addNewEventColor(calendarItem, isSingleEvent, color)
        } else {
            val eventColor = list[0]
            updateEventColor(eventColor, color)
        }
    }

    private fun addNewEventColor(calendarItem: CalendarItem, isSingleEvent: Boolean, color: Int) {
        eventColorDao.insert(
            EventColor(
                eventColorId = null,
                eventIdentifier = getEventIdentifier(calendarItem),
                eventNr = calendarItem.nr,
                isSingleEvent = isSingleEvent,
                color = color
            )
        )
    }

    private fun updateEventColor(eventColor: EventColor, color: Int) {
        eventColorDao.insert(
            EventColor(
                eventColorId = eventColor.eventColorId,
                eventIdentifier = eventColor.eventIdentifier,
                eventNr = eventColor.eventNr,
                isSingleEvent = eventColor.isSingleEvent,
                color = color
            )
        )
    }

    fun getColor(calendarItem: CalendarItem): Int {
        val customEventColor = getCustomEventColor(calendarItem)
        val colorResId =
            customEventColor?.color ?: getStandardColor(calendarItem)
        return getDisplayColorFromColor(ContextCompat.getColor(context, colorResId))
    }

    fun getResourceColor(calendarItem: CalendarItem): Int {
        val customEventColor = getCustomEventColor(calendarItem)
        return customEventColor?.color ?: getStandardColor(calendarItem)
    }

    private fun getCustomEventColor(calendarItem: CalendarItem): EventColor? {
        val eventIdentifier = getEventIdentifier(calendarItem)

        val singleEventColorList = eventColorDao.getByEventNrAndIdentifierAndIsSingleEvent(calendarItem.nr, eventIdentifier, true)
        if (singleEventColorList.isNotEmpty())
            return singleEventColorList[0]

        val eventColorList = eventColorDao.getByIdentifierAndIsSingleEvent(eventIdentifier, false)
        if (eventColorList.isNotEmpty())
            return eventColorList[0]
        return null
    }

    private fun getEventIdentifier(calendarItem: CalendarItem): String {
        return StringBuilder(calendarItem.title)
            .append(calendarItem.type.name)
            .append(calendarItem.url)
            .append(formatDateToHHmm(calendarItem.eventStart))
            .toString()
    }

    fun removeEventColor(eventNr: String) {
        eventColorDao.deleteByEventNr(eventNr)
    }

    companion object {
        fun getStandardColor(calendarItem: CalendarItem): Int {
            val colorResId = when (calendarItem.type) {
                CalendarItemType.LECTURE -> R.color.event_lecture
                CalendarItemType.EXERCISE -> R.color.event_exercise
                CalendarItemType.CANCELED -> R.color.event_canceled
                CalendarItemType.OTHER -> R.color.event_other
            }
            return colorResId
        }

        private fun formatDateToHHmm(data: DateTime): String {
            val timeFormat = DateTimeFormat.forPattern("HHmm").withLocale(Locale.US)
            return timeFormat.print(data)
        }
    }
}
