package de.tum.`in`.tumcampusapp.models.tumo

import de.tum.`in`.tumcampusapp.adapters.SimpleStickyListHeadersAdapter
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen".
 */

@Root(name = "row")
data class LecturesSearchRow(@field:Element
                             var dauer_info: String = "",
                             @field:Element(required = false)
                             var org_kennung_betreut: String = "",
                             @field:Element(required = false)
                             var org_name_betreut: String = "",
                             @field:Element(required = false)
                             var org_nr_betreut: Int = 0,
                             @field:Element
                             var semester: String = "",
                             @field:Element
                             var semester_id: String = "",
                             @field:Element
                             var semester_name: String = "",
                             @field:Element
                             var sj_name: String = "",
                             @field:Element
                             var stp_lv_art_kurz: String = "",
                             @field:Element
                             var stp_lv_art_name: String = "",
                             @field:Element(name = "stp_lv_nr")
                             var stp_lv_nr: String = "",
                             @field:Element
                             var stp_sp_nr: String = "",
                             @field:Element
                             var stp_sp_sst: String = "",
                             @field:Element(name = "stp_sp_titel")
                             var titel: String = "",
                             @field:Element(required = false)
                             var vortragende_mitwirkende: String = "")
    : Comparable<LecturesSearchRow>, SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun compareTo(other: LecturesSearchRow) = other.semester_id.compareTo(semester_id)

    override fun getHeadName() = semester_name

    override fun getHeaderId() = semester_id


    companion object {
        var STP_SP_NR = "stp_sp_nr"
    }
}