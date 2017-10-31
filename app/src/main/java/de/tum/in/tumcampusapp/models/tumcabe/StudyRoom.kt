package de.tum.`in`.tumcampusapp.models.tumcabe

import java.util.*

/**
 * Representation of a study room.
 */
data class StudyRoom(val id: Int, var code: String = "", var name: String = "", var location: String = "", val occupiedTill: Date) {
    override fun toString() = code
}
