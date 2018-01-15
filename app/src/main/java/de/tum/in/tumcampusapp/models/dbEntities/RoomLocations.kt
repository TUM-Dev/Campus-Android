package de.tum.`in`.tumcampusapp.models.dbEntities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName="room_locations")
data class RoomLocations(@PrimaryKey
                         var title: String = "",
                         var latitude: String = "",
                         var longtitude: String = "")