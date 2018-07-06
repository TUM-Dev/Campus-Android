package de.tum.`in`.tumcampusapp.component.tumui.grades.model

import android.content.Context
import android.support.v4.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter
import org.joda.time.DateTime
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * Exam passed by the user.
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "row", strict = false)
data class Exam(@field:Element(name = "lv_titel")
                var course: String = "",
                @field:Element(name = "lv_credits", required = false)
                var credits: String = "0",
                @field:Element(name = "datum", required = false)
                var date: DateTime = DateTime(),
                @field:Element(name = "pruefer_nachname", required = false)
                var examiner: String = "",
                @field:Element(name = "uninotenamekurz", required = false)
                var grade: String = "",
                @field:Element(name = "modus", required = false)
                var modus: String = "",
                @field:Element(name = "studienidentifikator")
                var programID: String = "",
                @field:Element(name = "lv_semester", required = false)
                var semester: String = "") : Comparable<Exam>, SimpleStickyListHeadersAdapter.SimpleStickyListItem {

    override fun getHeadName() = semester

    override fun getHeaderId() = semester

    override fun compareTo(other: Exam): Int =
            if (headerId == other.headerId) {
                course.compareTo(other.course) * (-1)
            } else {
                headerId.compareTo(other.headerId) * (-1)
            }

    fun getGradeColor(context: Context): Int {
        // While using getOrDefault() compiles, it results in a NoSuchMethodError on devices with
        // API levels lower than 24.
        val resId = GRADE_COLORS[grade] ?: R.color.grade_default
        return ContextCompat.getColor(context, resId)
    }

    companion object {
        private val GRADE_COLORS = mapOf(
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