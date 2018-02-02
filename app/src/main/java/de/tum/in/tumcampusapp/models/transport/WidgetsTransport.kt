package de.tum.`in`.tumcampusapp.models.transport

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "widgets_transport")
data class WidgetsTransport(@PrimaryKey
                            var id: Int = 0,
                            var station: String = "",
                            @ColumnInfo(name = "station_id")
                            var stationId: String = "",
                            var location: Boolean = false,
                            var reload: Boolean = false)