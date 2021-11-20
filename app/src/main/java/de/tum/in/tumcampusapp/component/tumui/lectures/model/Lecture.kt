package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.api.tumonline.converters.EscapedStringConverter
import de.tum.`in`.tumcampusapp.api.tumonline.converters.NullableEscapedStringConverter
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen".
 */
@Xml(name = "row")
data class Lecture(
    @PropertyElement(name = "dauer_info", converter = EscapedStringConverter::class) val duration: String = "",
    @PropertyElement(name = "org_kennung_betreut", converter = NullableEscapedStringConverter::class) val chairTumId: String? = null,
    @PropertyElement(name = "org_name_betreut", converter = NullableEscapedStringConverter::class) val chairName: String? = null,
    @PropertyElement(name = "org_nr_betreut") val chairId: Int? = null,
    @PropertyElement(name = "semester", converter = EscapedStringConverter::class) val semester: String,
    @PropertyElement(name = "semester_id", converter = EscapedStringConverter::class) val semesterId: String,
    @PropertyElement(name = "semester_name", converter = EscapedStringConverter::class) val semesterName: String,
    @PropertyElement(name = "sj_name", converter = EscapedStringConverter::class) val semesterYears: String,
    @PropertyElement(name = "stp_lv_art_kurz", converter = EscapedStringConverter::class) val shortLectureType: String,
    @PropertyElement(name = "stp_lv_art_name", converter = EscapedStringConverter::class) val lectureType: String,
    @PropertyElement(name = "stp_lv_nr", converter = EscapedStringConverter::class) val lectureId: String, // TODO: Rename variables
    @PropertyElement(name = "stp_sp_nr", converter = EscapedStringConverter::class) val stp_sp_nr: String,
    @PropertyElement(name = "stp_sp_sst", converter = EscapedStringConverter::class) val stp_sp_sst: String,
    @PropertyElement(name = "stp_sp_titel", converter = EscapedStringConverter::class) val title: String,
    @PropertyElement(name = "vortragende_mitwirkende", converter = NullableEscapedStringConverter::class) val lecturers: String? = null
) : Comparable<Lecture>, SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun compareTo(other: Lecture) = other.semesterId.compareTo(semesterId)

    override fun getHeadName() = semesterName

    override fun getHeaderId() = semesterId

    companion object {
        @JvmField val STP_SP_NR = "stp_sp_nr"
    }
}