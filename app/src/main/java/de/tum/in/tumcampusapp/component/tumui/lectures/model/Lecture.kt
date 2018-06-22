package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen".
 */
@Xml(name = "row")
data class Lecture(
        @PropertyElement(name = "dauer_info") var duration: String = "",  // TODO: TypeConverters
        @PropertyElement(name = "org_kennung_betreut") var chairTumId: String? = null,
        @PropertyElement(name = "org_name_betreut") var chairName: String? = null,
        @PropertyElement(name = "org_nr_betreut") var chairId: Int? = null,
        @PropertyElement(name = "semester") var semester: String,
        @PropertyElement(name = "semester_id") var semesterId: String,
        @PropertyElement(name = "semester_name") var semesterName: String,
        @PropertyElement(name = "sj_name") var semesterYears: String,
        @PropertyElement(name = "stp_lv_art_kurz") var shortLectureType: String,
        @PropertyElement(name = "stp_lv_art_name") var lectureType: String,
        @PropertyElement(name = "stp_lv_nr") var stp_lv_nr: String,   // TODO: Rename variables
        @PropertyElement(name = "stp_sp_nr") var stp_sp_nr: String,
        @PropertyElement(name = "stp_sp_sst") var stp_sp_sst: String,
        @PropertyElement(name = "stp_sp_titel") var title: String,
        @PropertyElement(name = "vortragende_mitwirkende") var lecturers: String? = null
) : Comparable<Lecture>, SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun compareTo(other: Lecture) = other.semesterId.compareTo(semesterId)

    override fun getHeadName() = semesterName

    override fun getHeaderId() = semesterId

    companion object {
        var STP_SP_NR = "stp_sp_nr"
    }

}