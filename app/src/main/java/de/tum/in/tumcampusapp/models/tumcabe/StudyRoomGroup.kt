package de.tum.`in`.tumcampusapp.models.tumcabe

/**
 * Representation of a study room group
 */
data class StudyRoomGroup(var id: Int = -1,
                          var name: String = "",
                          var details: String = "",
                          var rooms: List<StudyRoom> = mutableListOf()) : Comparable<StudyRoomGroup> {

    override fun toString() = name

    override fun compareTo(other: StudyRoomGroup) = name.compareTo(other.name)
}
