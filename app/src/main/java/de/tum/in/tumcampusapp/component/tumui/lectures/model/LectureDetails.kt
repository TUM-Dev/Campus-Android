package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "DetailsLehrveranstaltungen".
 */
@Xml(name = "row")
data class LectureDetails(
        @PropertyElement(name = "dauer_info") val duration: String? = null,
        @PropertyElement(name = "ersttermin") val firstAppointment: String? = null,
        @PropertyElement(name = "haupt_unterrichtssprache") val mainLanguage: String? = null,
        @PropertyElement(name = "lehrinhalt") val lectureContent: String? = null,
        @PropertyElement(name = "lehrmethode") val teachingMethod: String? = null,
        @PropertyElement(name = "lehrziel") val teachingTargets: String? = null,
        @PropertyElement(name = "org_kennung_betreut") val chairTumId: String? = null,
        @PropertyElement(name = "org_name_betreut") val chairName: String? = null,
        @PropertyElement(name = "org_nr_betreut") val chairId: String? = null,
        @PropertyElement(name = "semester") val semester: String? = null,
        @PropertyElement(name = "semester_id") val semesterId: String? = null,
        @PropertyElement(name = "semester_name") val semesterName: String? = null,
        @PropertyElement(name = "sj_name") val semesterYears: String? = null,
        @PropertyElement(name = "stp_lv_art_kurz") val shortLectureType: String? = null,
        @PropertyElement(name = "stp_lv_art_name") val lectureType: String? = null,
        @PropertyElement(name = "stp_lv_nr") val lectureId: String,  // TODO: Rename variables
        @PropertyElement(name = "stp_sp_nr") val stp_sp_nr: String,
        @PropertyElement(name = "stp_sp_sst") val stp_sp_sst: String? = null,
        @PropertyElement(name = "stp_sp_titel") val title: String,
        @PropertyElement(name = "studienbehelfe") val examinationAids: String? = null,
        @PropertyElement(name = "voraussetzung_lv") val lectureRequirements: String? = null,
        @PropertyElement(name = "vortragende_mitwirkende") val lecturers: String? = null
)
