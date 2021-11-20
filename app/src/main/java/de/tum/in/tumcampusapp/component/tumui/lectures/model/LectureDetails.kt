package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.api.tumonline.converters.EscapedStringConverter
import de.tum.`in`.tumcampusapp.api.tumonline.converters.NullableEscapedStringConverter

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "DetailsLehrveranstaltungen".
 */
@Xml(name = "row")
data class LectureDetails(
    @PropertyElement(name = "dauer_info", converter = NullableEscapedStringConverter::class) val duration: String? = null,
    @PropertyElement(name = "ersttermin", converter = NullableEscapedStringConverter::class) val firstAppointment: String? = null,
    @PropertyElement(name = "haupt_unterrichtssprache", converter = NullableEscapedStringConverter::class) val mainLanguage: String? = null,
    @PropertyElement(name = "lehrinhalt", converter = NullableEscapedStringConverter::class) val lectureContent: String? = null,
    @PropertyElement(name = "lehrmethode", converter = NullableEscapedStringConverter::class) val teachingMethod: String? = null,
    @PropertyElement(name = "lehrziel", converter = NullableEscapedStringConverter::class) val teachingTargets: String? = null,
    @PropertyElement(name = "org_kennung_betreut", converter = NullableEscapedStringConverter::class) val chairTumId: String? = null,
    @PropertyElement(name = "org_name_betreut", converter = NullableEscapedStringConverter::class) val chairName: String? = null,
    @PropertyElement(name = "org_nr_betreut", converter = NullableEscapedStringConverter::class) val chairId: String? = null,
    @PropertyElement(name = "semester", converter = NullableEscapedStringConverter::class) val semester: String? = null,
    @PropertyElement(name = "semester_id", converter = NullableEscapedStringConverter::class) val semesterId: String? = null,
    @PropertyElement(name = "semester_name", converter = NullableEscapedStringConverter::class) val semesterName: String? = null,
    @PropertyElement(name = "sj_name", converter = NullableEscapedStringConverter::class) val semesterYears: String? = null,
    @PropertyElement(name = "stp_lv_art_kurz", converter = NullableEscapedStringConverter::class) val shortLectureType: String? = null,
    @PropertyElement(name = "stp_lv_art_name", converter = NullableEscapedStringConverter::class) val lectureType: String? = null,
    @PropertyElement(name = "stp_lv_nr", converter = EscapedStringConverter::class) val lectureId: String, // TODO: Rename variables
    @PropertyElement(name = "stp_sp_nr", converter = EscapedStringConverter::class) val stp_sp_nr: String,
    @PropertyElement(name = "stp_sp_sst", converter = NullableEscapedStringConverter::class) val stp_sp_sst: String? = null,
    @PropertyElement(name = "stp_sp_titel", converter = EscapedStringConverter::class) val title: String,
    @PropertyElement(name = "studienbehelfe", converter = NullableEscapedStringConverter::class) val examinationAids: String? = null,
    @PropertyElement(name = "voraussetzung_lv", converter = NullableEscapedStringConverter::class) val lectureRequirements: String? = null,
    @PropertyElement(name = "vortragende_mitwirkende", converter = NullableEscapedStringConverter::class) val lecturers: String? = null
)
