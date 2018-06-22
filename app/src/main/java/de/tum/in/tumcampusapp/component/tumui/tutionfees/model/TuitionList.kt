package de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

/**
 * Wrapper class holding a list of tuitions ; based on ExamList
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Xml(name = "rowset")
data class TuitionList(@Element val tuitions: List<Tuition> = mutableListOf())
