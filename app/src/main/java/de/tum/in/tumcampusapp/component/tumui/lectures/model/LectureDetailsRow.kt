package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "DetailsLehrveranstaltungen".
 */
@Xml(name = "row")
data class LectureDetailsRow(
        @PropertyElement var dauer_info: String = "",
        @PropertyElement var ersttermin: String = "",  // TODO: Rename variables
        @PropertyElement var haupt_unterrichtssprache: String = "",
        @PropertyElement var lehrinhalt: String = "",
        @PropertyElement var lehrmethode: String = "",
        @PropertyElement var lehrziel: String = "",
        @PropertyElement var org_kennung_betreut: String = "",
        @PropertyElement var org_name_betreut: String = "",
        @PropertyElement var org_nr_betreut: String = "",
        @PropertyElement var semester: String = "",
        @PropertyElement var semester_id: String = "",
        @PropertyElement var semester_name: String = "",
        @PropertyElement var sj_name: String = "",
        @PropertyElement var stp_lv_art_kurz: String = "",
        @PropertyElement var stp_lv_art_name: String = "",
        @PropertyElement var stp_lv_nr: String = "",
        @PropertyElement var stp_sp_nr: String = "",
        @PropertyElement var stp_sp_sst: String = "",
        @PropertyElement var stp_sp_titel: String = "",
        @PropertyElement var studienbehelfe: String = "",
        @PropertyElement var voraussetzung_lv: String = "",
        @PropertyElement var vortragende_mitwirkende: String = ""
)
