package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.content.Context
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItemType
import de.tum.`in`.tumcampusapp.utils.ColorUtils.getDisplayColorFromColor

class EventColorProvider(private val context: Context) {

    fun getColor(calendarItem: CalendarItem): Int {
        val colorResId = when (calendarItem.type) {
            CalendarItemType.LECTURE -> R.color.event_lecture
            CalendarItemType.EXERCISE -> R.color.event_exercise
            CalendarItemType.CANCELED -> R.color.event_canceled
            CalendarItemType.OTHER -> R.color.event_other
        }
        return getDisplayColorFromColor(ContextCompat.getColor(context, colorResId))
    }

}
