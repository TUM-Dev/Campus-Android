package de.tum.`in`.tumcampusapp.component.ui.transportation.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings

@Entity(tableName = "transport_favorites" ,
        indices = [Index(value = ["symbol"], unique = true)])
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class TransportFavorites(@PrimaryKey(autoGenerate = true)
                              var id: Int = 0,
                              var symbol: String = "")
