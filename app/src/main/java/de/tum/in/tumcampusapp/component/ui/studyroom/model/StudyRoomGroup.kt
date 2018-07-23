package de.tum.`in`.tumcampusapp.component.ui.studyroom.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Representation of a study room group
 */
@Entity(tableName = "study_room_groups")
data class StudyRoomGroup(
        @PrimaryKey
        @SerializedName("id")
        var id: Int = -1,
        @SerializedName("name")
        var name: String = "",
        @SerializedName("details")
        var details: String = "",
        @Ignore
        @SerializedName("rooms")
        var rooms: List<StudyRoom> = emptyList()
) : Comparable<StudyRoomGroup> {

    override fun toString() = name

    override fun compareTo(other: StudyRoomGroup) = name.compareTo(other.name)

}
