package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "TermineLehrveranstaltungen".
 *
 * @see LectureAppointmentsRow
 *
 * @see [SimpleXML tutorial](http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php)
 */
@Root(name = "rowset")
data class LectureAppointmentsRowSet(@field:ElementList(inline = true, required = false)
                                     var lehrveranstaltungenTermine: List<LectureAppointmentsRow> = emptyList())
