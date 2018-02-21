package de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * Wrapper class holding a list of tuitions ; based on ExamList
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "rowset")
data class TuitionList(@field:ElementList(inline = true) var tuitions: List<Tuition> = mutableListOf())
