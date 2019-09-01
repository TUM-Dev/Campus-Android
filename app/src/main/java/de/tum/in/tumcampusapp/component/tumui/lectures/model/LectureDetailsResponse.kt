package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "DetailsLehrveranstaltungen"
 *
 * @see LectureDetails
 */
@Xml(name = "rowset")
data class LectureDetailsResponse(
    @Element val lectureDetails: List<LectureDetails> = mutableListOf()
)
