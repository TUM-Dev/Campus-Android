package de.tum.`in`.tumcampusapp.component.ui.studyroom.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

/**
 * Representation of a study room group
 */
@Entity(tableName = "study_room_groups")
data class StudyRoomGroup(@field:PrimaryKey
                          var id: Int = -1,
                          var name: String = "",
                          var details: String = "",
                          @Ignore
                          var rooms: List<StudyRoom> = mutableListOf()) : Comparable<StudyRoomGroup> {

    override fun toString() = name

    override fun compareTo(other: StudyRoomGroup) = name.compareTo(other.name)
}
