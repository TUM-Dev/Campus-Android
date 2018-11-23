package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo

/**
 * Entity for lecture room locations
 */
@Entity(tableName = "room_locations")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class RoomLocation(
        @PrimaryKey
        val title: String,
        val latitude: String,
        val longitude: String
) {

    constructor(title: String, geo: Geo) : this(title, geo.latitude, geo.longitude)

    fun toGeo() = Geo(latitude, longitude)

}
