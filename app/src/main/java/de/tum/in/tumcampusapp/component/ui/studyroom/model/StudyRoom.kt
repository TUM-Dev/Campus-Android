package de.tum.`in`.tumcampusapp.component.ui.studyroom.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
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
    @ColumnInfo(name = "building_name")
    @SerializedName("building_name")
    var buildingName: String = "",
    @ColumnInfo(name = "group_id")
    @SerializedName("group_id")
    var studyRoomGroup: Int = -1,
    @ColumnInfo(name = "occupied_until")
    @SerializedName("occupied_until")
    var occupiedUntil: DateTime? = null,
    @ColumnInfo(name = "free_until")
    @SerializedName("free_until")
    var freeUntil: DateTime? = null
) : Comparable<StudyRoom> {

    override fun compareTo(other: StudyRoom): Int {
        // We use the following sorting order:
        // 1. Rooms that are currently free and don't have a reservation coming up (freeUntil == null)
        // 2. Rooms that are currently free but have a reservation coming up (sorted descending by
        //    the amount of free time remaining)
        // 3. Rooms that are currently occupied but will be free soon (sorted ascending by the
        //    amount of occupied time remaining)
        // 4. The remaining rooms
        return compareBy<StudyRoom> { it.freeUntil?.millis?.times(-1) }
                .thenBy { it.occupiedUntil }
                .thenBy { it.name }
                .compare(this, other)
    }

    override fun toString() = code
}
