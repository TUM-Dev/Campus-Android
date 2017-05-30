package de.tum.`in`.tumcampusapp.models.tumcabe

/**
 * Representation of a study room group
 */
class StudyRoomGroup(val id: Int, name: String, details: String, val rooms: List<StudyRoom>) : Comparable<StudyRoomGroup> {
    var name = ""
    var details = ""

    init {
        this.name = name
        this.details = details
    }

    override fun toString(): String {
        return name
    }

    override fun compareTo(studyRoomGroup: StudyRoomGroup): Int {
        return name.compareTo(studyRoomGroup.name)
    }
}
