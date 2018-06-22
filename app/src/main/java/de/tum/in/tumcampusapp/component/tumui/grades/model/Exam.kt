package de.tum.`in`.tumcampusapp.component.tumui.grades.model

import android.content.Context
import android.support.v4.content.ContextCompat
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter
import de.tum.`in`.tumcampusapp.utils.compareTo
import java.util.*

/**
 * Exam passed by the user.
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Xml(name = "row")
data class Exam(
        @PropertyElement(name = "lv_titel")
        val course: String,
        @PropertyElement(name = "lv_credits")
        val credits: String? = null,
        @PropertyElement(name = "datum")
        val date: Date? = null,
        @PropertyElement(name = "pruefer_nachname")
        val examiner: String? = null,
        @PropertyElement(name = "uninotenamekurz")
        val grade: String? = null,
        @PropertyElement(name = "modus")
        val modus: String? = null,
        @PropertyElement(name = "studienidentifikator")
        val programID: String,
        @PropertyElement(name = "lv_semester")
        val semester: String? = null
) : Comparable<Exam>, SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun getHeadName() = semester

    override fun getHeaderId() = semester

    override fun compareTo(other: Exam): Int {
        if (this.semester == other.semester) {
            return course.compareTo(other.course) * (-1)
        } else {
            return semester?.compareTo(other.semester) ?: 1  // TODO
        }
    }

    fun getGradeColor(c: Context): Int {
        return if (grade != null) {
            val resourceId = GRADE_COLOR.getOrDefault(grade!!, R.color.grade_default)
            return ContextCompat.getColor(c, resourceId)
        } else {
            R.color.grade_default
        }
    }

    companion object {
        private val GRADE_COLOR = mapOf(
                "1,0" to R.color.grade_1_0,
                "1,3" to R.color.grade_1_3,
                "1,4" to R.color.grade_1_3,
                "1,7" to R.color.grade_1_7,
                "2,0" to R.color.grade_2_0,
                "2,3" to R.color.grade_2_3,
                "2,4" to R.color.grade_2_3,
                "2,7" to R.color.grade_2_7,
                "3,0" to R.color.grade_3_0,
                "3,3" to R.color.grade_3_3,
                "3,4" to R.color.grade_3_3,
                "3,7" to R.color.grade_3_7,
                "4,0" to R.color.grade_4_0,
                "4,3" to R.color.grade_4_3,
                "4,4" to R.color.grade_4_3,
                "4,7" to R.color.grade_4_7,
                "5,0" to R.color.grade_5_0
        )
    }
}