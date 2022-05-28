package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.content.Context
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItemType
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.EventColor
import de.tum.`in`.tumcampusapp.utils.ColorUtils.getDisplayColorFromColor
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class EventColorProvider(
        private val context: Context,
        private val eventColorDao: EventColorDao,
) {

    fun changeEventColor(calendarItem: CalendarItem, color: Int, isSingleEvent: Boolean = false) {
        if (isSingleEvent) {
            val list = eventColorDao.getByEventNr(calendarItem.nr, getEventIdentifier(calendarItem), true)
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

    private fun updateEventColor(eventColor: EventColor, color: Int) {
        eventColorDao.insert(EventColor(
                eventColorId = eventColor.eventColorId,
                eventIdentifier = eventColor.eventIdentifier,
                eventNr = eventColor.eventNr,
                isSingleEvent = eventColor.isSingleEvent,
                color = color
        ))
    }

    private fun addNewEventColor(calendarItem: CalendarItem, isSingleEvent: Boolean, color: Int) {
        eventColorDao.insert(EventColor(
                eventColorId = null,
                eventIdentifier = getEventIdentifier(calendarItem),
                eventNr = calendarItem.nr,
                isSingleEvent = isSingleEvent,
                color = color
        ))
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
        // TODO refactor
        // get by eventNr and identifier -> if not empty then return [0]
        // get by identifier -> if not empty then return [0] else return null
        val customEventColors = eventColorDao.getByEventIdentifier(eventIdentifier)

        if (customEventColors.isEmpty()) return null

        if (customEventColors.size > 1) {
            val singleCustomColor = customEventColors.filter { it.eventNr == calendarItem.nr }
            if (singleCustomColor.isNotEmpty()) return singleCustomColor[0]
        }

        val customEventColor = customEventColors.filter { !it.isSingleEvent }
        if (customEventColor.isEmpty())
            return null
        return customEventColor[0]
    }

    companion object {
        fun getStandardColor(calendarItem: CalendarItem): Int {
            val colorResId = when (calendarItem.type) {
                CalendarItemType.LECTURE -> R.color.event_lecture
                CalendarItemType.EXERCISE -> R.color.event_exercise
                CalendarItemType.CANCELED -> R.color.event_canceled
                CalendarItemType.OTHER -> R.color.event_other
            }
            return colorResId;
        }

        fun getEventIdentifier(calendarItem: CalendarItem): String {
            return StringBuilder(calendarItem.title)
                    .append(calendarItem.type.name)
                    .append(calendarItem.url)
                    .append(formatDateToHHmm(calendarItem.eventStart))
                    .toString()
        }

        private fun formatDateToHHmm(data: DateTime): String {
            val timeFormat = DateTimeFormat.forPattern("HHmm").withLocale(Locale.US)
            return timeFormat.print(data)
        }
    }
}
