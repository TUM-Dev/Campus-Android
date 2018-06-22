package de.tum.`in`.tumcampusapp.component.tumui.grades.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

/**
 * Wrapper class holding a list of exams.
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Xml(name = "rowset")
data class ExamList(@Element val exams: List<Exam> = mutableListOf())
