package de.tum.`in`.tumcampusapp.component.other.locations.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings

@Entity
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class BuildingToGps(@PrimaryKey
                         var id: String = "",
                         var latitude: String = "",
                         var longitude: String = "")
