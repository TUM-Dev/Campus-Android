package de.tum.`in`.tumcampusapp.models.tumcabe

/**
 * Representation of a study room group
 */
data class StudyRoomGroup(val id: Int, var name: String = "", var details: String = "", val rooms: List<StudyRoom>) : Comparable<StudyRoomGroup> {

    override fun toString() = name

    override fun compareTo(other: StudyRoomGroup) = name.compareTo(other.name)
}
