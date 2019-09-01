package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.api.tumonline.converters.DateTimeConverter
import org.joda.time.DateTime

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "TermineLehrveranstaltungen".
 */
@Xml(name = "row")
data class LectureAppointment(
    @PropertyElement(name = "art") val type: String? = null,
    @PropertyElement(name = "beginn_datum_zeitpunkt", converter = DateTimeConverter::class) val startTime: DateTime,
    @PropertyElement(name = "ende_datum_zeitpunkt", converter = DateTimeConverter::class) val endTime: DateTime,
    @PropertyElement(name = "ort") val location: String? = null,
    @PropertyElement(name = "raum_nr") val roomNumber: String? = null,
    @PropertyElement(name = "raum_nr_architekt") val roomNumberArchitect: String? = null,
    @PropertyElement(name = "termin_betreff") val title: String? = null,
    @PropertyElement(name = "lv_grp_nr") val lectureGroupId: String? = null,
    @PropertyElement(name = "lv_grp_name") val lectureGroupName: String? = null
)