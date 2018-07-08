package de.tum.`in`.tumcampusapp.component.other.locations.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings

@Entity
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class BuildingToGps(@PrimaryKey
                         var id: String = "",
                         var latitude: String = "",
                         var longitude: String = "")
