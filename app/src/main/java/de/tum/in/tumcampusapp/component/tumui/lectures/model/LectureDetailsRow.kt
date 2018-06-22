package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "DetailsLehrveranstaltungen".
 */
@Xml(name = "row")
data class LectureDetailsRow(@Attribute var dauer_info: String = "",
                             @Attribute var ersttermin: String = "",  // TODO: Rename variables
                             @Attribute var haupt_unterrichtssprache: String = "",
                             @Attribute var lehrinhalt: String = "",
                             @Attribute var lehrmethode: String = "",
                             @Attribute var lehrziel: String = "",
                             @Attribute var org_kennung_betreut: String = "",
                             @Attribute var org_name_betreut: String = "",
                             @Attribute var org_nr_betreut: String = "",
                             @Attribute var semester: String = "",
                             @Attribute var semester_id: String = "",
                             @Attribute var semester_name: String = "",
                             @Attribute var sj_name: String = "",
                             @Attribute var stp_lv_art_kurz: String = "",
                             @Attribute var stp_lv_art_name: String = "",
                             @Attribute var stp_lv_nr: String = "",
                             @Attribute var stp_sp_nr: String = "",
                             @Attribute var stp_sp_sst: String = "",
                             @Attribute var stp_sp_titel: String = "",
                             @Attribute var studienbehelfe: String = "",
                             @Attribute var voraussetzung_lv: String = "",
                             @Attribute var vortragende_mitwirkende: String = "")
