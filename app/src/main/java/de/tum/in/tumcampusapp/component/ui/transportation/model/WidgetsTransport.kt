package de.tum.`in`.tumcampusapp.component.ui.transportation.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings

@Entity(tableName = "widgets_transport")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class WidgetsTransport(@PrimaryKey
                            var id: Int = 0,
                            var station: String = "",
                            @ColumnInfo(name = "station_id")
                            var stationId: String = "",
                            var location: Boolean = false,
                            var reload: Boolean = false)