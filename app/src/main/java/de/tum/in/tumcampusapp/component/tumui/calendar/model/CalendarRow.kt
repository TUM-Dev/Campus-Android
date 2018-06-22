package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo

@Xml(name = "event")
data class CalendarRow(
        @PropertyElement var description: String = "",
        @PropertyElement var dtend: String = "",
        @PropertyElement var dtstart: String = "",
        @PropertyElement var geo: Geo? = null,
        @PropertyElement var location: String = "",
        @PropertyElement var nr: String = "",
        @PropertyElement var status: String = "",
        @PropertyElement var title: String = "",
        @PropertyElement var url: String = "") {
    /**
     * Retrieve related values for calendar item as CalendarItem object
     */
    fun toCalendarItem(): CalendarItem {
        return CalendarItem(nr, status, url, title, description, dtstart,
                dtend, location, false)
    }
}
