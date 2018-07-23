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
        @SerializedName("raum_nr")
        var id: Int = -1,
        @SerializedName("raum_code")
        var code: String = "",
        @SerializedName("raum_name")
        var name: String = "",
        @SerializedName("gebaeude_name")
        var location: String = "",
        @ColumnInfo(name = "group_id")
        var studyRoomGroup: Int = -1,
        @ColumnInfo(name = "occupied_till")
        @SerializedName("belegung_bis")
        var occupiedTill: DateTime = DateTime()
) : Comparable<StudyRoom> {

    override fun compareTo(other: StudyRoom): Int {
        return compareValuesBy(this, other, { it.occupiedTill }, { it.name })
    }

    override fun toString() = code

}
