package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "DetailsLehrveranstaltungen".
 */
@Xml(name = "row")
data class LectureDetails(
        @PropertyElement(name = "dauer_info") var duration: String? = null,
        @PropertyElement(name = "ersttermin") var firstAppointment: String? = null, // TODO: TypeConverters
        @PropertyElement(name = "haupt_unterrichtssprache") var mainLanguage: String? = null,
        @PropertyElement(name = "lehrinhalt") var lectureContent: String? = null,
        @PropertyElement(name = "lehrmethode") var teachingMethod: String? = null,
        @PropertyElement(name = "lehrziel") var teachingTargets: String? = null,
        @PropertyElement(name = "org_kennung_betreut") var chairTumId: String? = null,
        @PropertyElement(name = "org_name_betreut") var chairName: String? = null,
        @PropertyElement(name = "org_nr_betreut") var chairId: String? = null,
        @PropertyElement(name = "semester") var semester: String? = null,
        @PropertyElement(name = "semester_id") var semesterId: String? = null,
        @PropertyElement(name = "semester_name") var semesterName: String? = null,
        @PropertyElement(name = "sj_name") var semesterYears: String? = null,
        @PropertyElement(name = "stp_lv_art_kurz") var shortLectureType: String? = null,
        @PropertyElement(name = "stp_lv_art_name") var lectureType: String? = null,
        @PropertyElement(name = "stp_lv_nr") var stp_lv_nr: String,  // TODO: Rename variables
        @PropertyElement(name = "stp_sp_nr") var stp_sp_nr: String,
        @PropertyElement(name = "stp_sp_sst") var stp_sp_sst: String? = null,
        @PropertyElement(name = "stp_sp_titel") var title: String,
        @PropertyElement(name = "studienbehelfe") var examinationAids: String? = null,
        @PropertyElement(name = "voraussetzung_lv") var lectureRequirements: String? = null,
        @PropertyElement(name = "vortragende_mitwirkende") var lecturers: String? = null
)
