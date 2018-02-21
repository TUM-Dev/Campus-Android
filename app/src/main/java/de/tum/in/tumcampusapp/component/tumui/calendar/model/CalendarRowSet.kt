package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen" or "eigeneLehrveranstaltungen".
 *
 * @see LecturesSearchRow
 *
 * @see [SimpleXML tutorial](http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php)
 */
@Root(name = "events")
data class CalendarRowSet(@field:ElementList(inline = true, required = false) var kalendarList: List<CalendarRow> = mutableListOf())
