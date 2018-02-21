package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen" or "eigeneLehrveranstaltungen".
 *
 * @see LecturesSearchRow
 */
@Root(name = "rowset")
data class LecturesSearchRowSet(@field:ElementList(inline = true, required = false)
                                var lehrveranstaltungen: List<LecturesSearchRow> = mutableListOf())
