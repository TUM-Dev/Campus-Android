package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * Class holding tuition information.
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "row", strict = false)
data class Tuition(@field:Element(name = "frist")
                   var frist: String = "",
                   @field:Element(name = "semester_bezeichnung")
                   var semesterBez: String = "",
                   @field:Element(name = "soll")
                   var soll: String = "")
