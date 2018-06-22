package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo

@Xml(name = "event")
data class Event(
        @PropertyElement(name = "description") var description: String? = null,
        @PropertyElement(name = "dtend") var endTime: String? = null,     // TODO: TypeConverters
        @PropertyElement(name = "dtstart") var startTime: String? = null,
        @PropertyElement(name = "geo") var geo: Geo? = null,
        @PropertyElement(name = "location") var location: String? = null,
        @PropertyElement(name = "nr") var id: String? = null,
        @PropertyElement(name = "status") var status: String? = null,
        @PropertyElement(name = "title") var title: String,
        @PropertyElement(name = "url") var url: String? = null) {
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
