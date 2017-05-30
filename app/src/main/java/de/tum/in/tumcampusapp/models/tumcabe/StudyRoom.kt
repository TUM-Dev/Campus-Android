package de.tum.`in`.tumcampusapp.models.tumcabe

import java.util.*

/**
 * Representation of a study room.
 */
class StudyRoom(val id: Int, code: String, name: String, location: String, val occupiedTill: Date) {
    var code = ""
    var name = ""
    var location = ""

    init {
        this.code = code
        this.name = name
        this.location = location
    }

    override fun toString(): String {
        return code
    }
}
