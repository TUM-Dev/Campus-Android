package de.tum.`in`.tumcampusapp.component.ui.transportation.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings

@Entity(tableName = "transport_favorites" ,
        indices = [Index(value = ["symbol"], unique = true)])
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class TransportFavorites(@PrimaryKey(autoGenerate = true)
                              var id: Int = 0,
                              var symbol: String = "")
