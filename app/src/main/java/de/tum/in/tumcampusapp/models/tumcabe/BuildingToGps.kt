package de.tum.`in`.tumcampusapp.models.tumcabe

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class BuildingToGps(@PrimaryKey
                         var id: String = "",
                         var latitude: String = "",
                         var longitude: String = "")
