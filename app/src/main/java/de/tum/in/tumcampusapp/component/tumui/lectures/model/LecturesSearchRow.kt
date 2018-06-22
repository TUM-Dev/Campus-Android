package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen".
 */
@Xml(name = "row")
data class LecturesSearchRow(
        @Attribute var dauer_info: String = "",  // TODO: Rename variable
        @Attribute var org_kennung_betreut: String = "",
        @Attribute var org_name_betreut: String = "",
        @Attribute var org_nr_betreut: Int = 0,
        @Attribute var semester: String = "",
        @Attribute var semester_id: String = "",
        @Attribute var semester_name: String = "",
        @Attribute var sj_name: String = "",
        @Attribute var stp_lv_art_kurz: String = "",
        @Attribute var stp_lv_art_name: String = "",
        @Attribute(name = "stp_lv_nr") var stp_lv_nr: String = "",
        @Attribute var stp_sp_nr: String = "",
        @Attribute var stp_sp_sst: String = "",
        @Attribute(name = "stp_sp_titel") var titel: String = "",
        @Attribute var vortragende_mitwirkende: String = ""
) : Comparable<LecturesSearchRow>, SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun compareTo(other: LecturesSearchRow) = other.semester_id.compareTo(semester_id)

    override fun getHeadName() = semester_name

    override fun getHeaderId() = semester_id


    companion object {
        var STP_SP_NR = "stp_sp_nr"
    }
}