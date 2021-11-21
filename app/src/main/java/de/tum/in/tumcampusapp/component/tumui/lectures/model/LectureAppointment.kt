package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.api.tumonline.converters.DateTimeConverter
import de.tum.`in`.tumcampusapp.api.tumonline.converters.NullableEscapedStringConverter
import org.joda.time.DateTime

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "TermineLehrveranstaltungen".
 */
@Xml(name = "row")
data class LectureAppointment(
    @PropertyElement(name = "art", converter = NullableEscapedStringConverter::class) val type: String? = null,
    @PropertyElement(name = "beginn_datum_zeitpunkt", converter = DateTimeConverter::class) val startTime: DateTime,
    @PropertyElement(name = "ende_datum_zeitpunkt", converter = DateTimeConverter::class) val endTime: DateTime,
    @PropertyElement(name = "ort", converter = NullableEscapedStringConverter::class) val location: String? = null,
    @PropertyElement(name = "raum_nr", converter = NullableEscapedStringConverter::class) val roomNumber: String? = null,
    @PropertyElement(name = "raum_nr_architekt", converter = NullableEscapedStringConverter::class) val roomNumberArchitect: String? = null,
    @PropertyElement(name = "termin_betreff", converter = NullableEscapedStringConverter::class) val title: String? = null,
    @PropertyElement(name = "lv_grp_nr", converter = NullableEscapedStringConverter::class) val lectureGroupId: String? = null,
    @PropertyElement(name = "lv_grp_name", converter = NullableEscapedStringConverter::class) val lectureGroupName: String? = null
)