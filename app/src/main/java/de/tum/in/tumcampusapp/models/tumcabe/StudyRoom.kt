package de.tum.`in`.tumcampusapp.models.tumcabe

import java.util.*

/**
 * Representation of a study room.
 */
data class StudyRoom(var id: Int = -1,
                     var code: String = "",
                     var name: String = "",
                     var location: String = "",
                     var occupiedTill: Date = Date()) {
    override fun toString() = code
}
