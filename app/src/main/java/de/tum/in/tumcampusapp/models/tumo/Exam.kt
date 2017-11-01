package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.util.*

/**
 * Exam passed by the user.
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "row", strict = false)
data class Exam(@field:Element(name = "lv_titel")
                var course: String = "",
                @field:Element(name = "lv_credits", required = false)
                var credits: String = "0",
                @field:Element(name = "datum", required = false)
                var date: Date = Date(),
                @field:Element(name = "pruefer_nachname", required = false)
                var examiner: String = "",
                @field:Element(name = "uninotenamekurz")
                var grade: String = "",
                @field:Element(name = "modus", required = false)
                var modus: String = "",
                @field:Element(name = "studienidentifikator")
                var programID: String = "",
                @field:Element(name = "lv_semester", required = false)
                var semester: String = "")