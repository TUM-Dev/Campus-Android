package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo

@Xml(name = "event")
data class CalendarRow(
        @Attribute var description: String = "",
        @Attribute var dtend: String = "",
        @Attribute var dtstart: String = "",
        @Attribute var geo: Geo? = null,
        @Attribute var location: String = "",
        @Attribute var nr: String = "",
        @Attribute var status: String = "",
        @Attribute var title: String = "",
        @Attribute var url: String = "") {
    /**
     * Retrieve related values for calendar item as CalendarItem object
     */
    fun toCalendarItem(): CalendarItem {
        return CalendarItem(nr, status, url, title, description, dtstart,
                dtend, location, false)
    }
}
