package de.tum.`in`.tumcampusapp.utils.sync.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings
import org.joda.time.DateTime

@Entity
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class Sync(@PrimaryKey
                var id: String = "",
                var lastSync: DateTime = DateTime())