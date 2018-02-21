package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "event")
data class CalendarRow(
        @field:Element(required = false) var description: String = "",
        @field:Element(required = false) var dtend: String = "",
        @field:Element(required = false) var dtstart: String = "",
        @field:Element(required = false) var geo: Geo? = null,
        @field:Element(required = false) var location: String = "",
        @field:Element(required = false) var nr: String = "",
        @field:Element(required = false) var status: String = "",
        @field:Element(required = false) var title: String = "",
        @field:Element(required = false) var url: String = "") {
    /**
     * Retrieve related values for calendar item as CalendarItem object
     */
    fun toCalendarItem(): CalendarItem {
        return CalendarItem(nr, status, url, title, description, dtstart,
                dtend, location, false)
    }
}
