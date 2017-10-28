package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "DetailsLehrveranstaltungen"
 *
 * @see LectureDetailsRow
 *
 * @see [SimpleXML tutorial](http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php)
 */
@Root(name = "rowset")
data class LectureDetailsRowSet(@field:ElementList(inline = true)
                                var lehrveranstaltungenDetails: List<LectureDetailsRow> = mutableListOf())
