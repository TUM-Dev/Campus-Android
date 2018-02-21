package de.tum.`in`.tumcampusapp.models.transport

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "transport_favorites" ,
        indices = [Index(value = "symbol", unique = true)])
data class TransportFavorites(@PrimaryKey(autoGenerate = true)
                              var id: Int = 0,
                              var symbol: String = "")
