package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "DetailsLehrveranstaltungen".
 *
 * @see [SimpleXML tutorial](http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php)
 */
@Root(name = "row", strict = false)
data class LectureDetailsRow(@field:Element(required = false) var dauer_info: String = "",
                             @field:Element(required = false) var ersttermin: String = "",
                             @field:Element(required = false) var haupt_unterrichtssprache: String = "",
                             @field:Element(required = false) var lehrinhalt: String = "",
                             @field:Element(required = false) var lehrmethode: String = "",
                             @field:Element(required = false) var lehrziel: String = "",
                             @field:Element(required = false) var org_kennung_betreut: String = "",
                             @field:Element(required = false) var org_name_betreut: String = "",
                             @field:Element(required = false) var org_nr_betreut: String = "",
                             @field:Element(required = false) var semester: String = "",
                             @field:Element(required = false) var semester_id: String = "",
                             @field:Element(required = false) var semester_name: String = "",
                             @field:Element(required = false) var sj_name: String = "",
                             @field:Element(required = false) var stp_lv_art_kurz: String = "",
                             @field:Element(required = false) var stp_lv_art_name: String = "",
                             @field:Element var stp_lv_nr: String = "",
                             @field:Element var stp_sp_nr: String = "",
                             @field:Element(required = false) var stp_sp_sst: String = "",
                             @field:Element var stp_sp_titel: String = "",
                             @field:Element(required = false) var studienbehelfe: String = "",
                             @field:Element(required = false) var voraussetzung_lv: String = "",
                             @field:Element(required = false) var vortragende_mitwirkende: String = "")
