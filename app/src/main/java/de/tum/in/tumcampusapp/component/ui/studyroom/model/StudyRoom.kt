package de.tum.`in`.tumcampusapp.component.ui.studyroom.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

/**
 * Representation of a study room.
 */
@Entity(tableName = "study_rooms")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class StudyRoom(
        @PrimaryKey
        @SerializedName("room_id")
        var id: Int = -1,
        @SerializedName("room_code")
        var code: String = "",
        @SerializedName("room_name")
        var name: String = "",
        @SerializedName("building_name")
        var location: String = "",
        @ColumnInfo(name = "group_id")
        @SerializedName("group_id")
        var studyRoomGroup: Int = -1,
        @ColumnInfo(name = "occupied_till")
        @SerializedName("occupied_until")
        var occupiedTill: DateTime = DateTime()
) : Comparable<StudyRoom> {

    override fun compareTo(other: StudyRoom): Int {
        return compareValuesBy(this, other, { it.occupiedTill }, { it.name })
    }

    override fun toString() = code

}
