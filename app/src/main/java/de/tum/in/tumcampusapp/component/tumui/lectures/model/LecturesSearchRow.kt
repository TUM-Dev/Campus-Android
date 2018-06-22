package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen".
 */
@Xml(name = "row")
data class LecturesSearchRow(
        @PropertyElement var dauer_info: String = "",  // TODO: Rename variable
        @PropertyElement var org_kennung_betreut: String = "",
        @PropertyElement var org_name_betreut: String = "",
        @PropertyElement var org_nr_betreut: Int = 0,
        @PropertyElement var semester: String = "",
        @PropertyElement var semester_id: String = "",
        @PropertyElement var semester_name: String = "",
        @PropertyElement var sj_name: String = "",
        @PropertyElement var stp_lv_art_kurz: String = "",
        @PropertyElement var stp_lv_art_name: String = "",
        @PropertyElement(name = "stp_lv_nr") var stp_lv_nr: String = "",
        @PropertyElement var stp_sp_nr: String = "",
        @PropertyElement var stp_sp_sst: String = "",
        @PropertyElement(name = "stp_sp_titel") var titel: String = "",
        @PropertyElement var vortragende_mitwirkende: String = ""
) : Comparable<LecturesSearchRow>, SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun compareTo(other: LecturesSearchRow) = other.semester_id.compareTo(semester_id)

    override fun getHeadName() = semester_name

    override fun getHeaderId() = semester_id


    companion object {
        var STP_SP_NR = "stp_sp_nr"
    }
}