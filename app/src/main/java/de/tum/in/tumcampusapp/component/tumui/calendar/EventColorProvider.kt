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

    fun getColor(calendarItem: CalendarItem): Int {
        val customEventColor = getCustomEventColor(calendarItem)
        val colorResId =
                customEventColor?.color ?: getStandardColor(calendarItem)
        return getDisplayColorFromColor(ContextCompat.getColor(context, colorResId))
    }

    private fun getCustomEventColor(calendarItem: CalendarItem): EventColor? {
        val eventIdentifier = getEventIdentifier(calendarItem)
        val customEventColors = eventColorDao.getByEventIdentifier(eventIdentifier)

        if (customEventColors.isEmpty()) return null

        if (customEventColors.size > 1) {
            val singleCustomColor = customEventColors.filter { it.eventNr == calendarItem.nr }
            if (singleCustomColor.isNotEmpty()) return singleCustomColor[0]
        }

        val customEventColor = customEventColors[0]
        if (customEventColor.isSingleEvent && customEventColor.eventNr != calendarItem.nr)
            return null
        return customEventColor
    }

    private fun getStandardColor(calendarItem: CalendarItem): Int {
        val colorResId = when (calendarItem.type) {
            CalendarItemType.LECTURE -> R.color.event_lecture
            CalendarItemType.EXERCISE -> R.color.event_exercise
            CalendarItemType.CANCELED -> R.color.event_canceled
            CalendarItemType.OTHER -> R.color.event_other
        }
        return colorResId;
    }

    companion object {
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
