package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen" or "eigeneLehrveranstaltungen".
 *
 * @see LecturesSearchRow
 *
 * @see [SimpleXML tutorial](http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php)
 */
@Xml(name = "events")
data class CalendarRowSet(@Element var kalendarList: List<CalendarRow>? = null) // TODO: Rename variable
