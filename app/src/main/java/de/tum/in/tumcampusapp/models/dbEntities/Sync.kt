package de.tum.`in`.tumcampusapp.models.dbEntities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Sync(@PrimaryKey
                var id: String = "",
                var lastSync: String = "")