package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo

@Xml(name = "event")
data class Event(
        @PropertyElement(name = "description") val description: String? = null,
        @PropertyElement(name = "dtend") val endTime: String? = null,     // TODO: TypeConverters
        @PropertyElement(name = "dtstart") val startTime: String? = null,
        @PropertyElement(name = "geo") val geo: Geo? = null,
        @PropertyElement(name = "location") val location: String? = null,
        @PropertyElement(name = "nr") val id: String? = null,
        @PropertyElement(name = "status") val status: String? = null,
        @PropertyElement(name = "title") val title: String,
        @PropertyElement(name = "url") val url: String? = null) {
    /**
     * Retrieve related values for calendar item as CalendarItem object
     */
    fun toCalendarItem(): CalendarItem {
        return CalendarItem(
                id ?: "", status ?: "", url ?: "", title,
                description ?: "", startTime ?: "",
                endTime ?: "", location ?: "", false
        )
    }
}
