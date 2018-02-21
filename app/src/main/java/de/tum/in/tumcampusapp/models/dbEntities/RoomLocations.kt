package de.tum.`in`.tumcampusapp.models.dbEntities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import de.tum.`in`.tumcampusapp.models.tumo.Geo

/**
 * Entity for lecture room locations
 */
@Entity(tableName="room_locations")
data class RoomLocations(@PrimaryKey
                         var title: String = "",
                         var latitude: String = "",
                         var longitude: String = "") {
    constructor(title: String, geo: Geo): this(title, geo.latitude, geo.longitude)

    /**
     * Retrieve Geo object with related information plugged
     */
    fun toGeo(): Geo {
        return Geo(latitude, longitude);
    }
}