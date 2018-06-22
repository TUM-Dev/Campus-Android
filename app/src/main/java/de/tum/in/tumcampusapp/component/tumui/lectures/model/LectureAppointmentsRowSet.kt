package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "TermineLehrveranstaltungen".
 *
 * @see LectureAppointmentsRow
 *
 * @see [SimpleXML tutorial](http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php)
 */
@Xml(name = "rowset")
data class LectureAppointmentsRowSet(
        @Element var lehrveranstaltungenTermine: List<LectureAppointmentsRow> = mutableListOf() // TODO: Rename variable
)
