package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen" or "eigeneLehrveranstaltungen".
 *
 * @see LecturesSearchRow
 */
@Xml(name = "events")
data class Events(@Element val events: List<Event>? = null)
