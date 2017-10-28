package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * Wrapper class holding a list of exams.
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "rowset")
data class ExamList(@field:ElementList(inline = true)
                    var exams: List<Exam> = mutableListOf())
