package de.tum.`in`.tumcampusapp.component.notifications.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings

@Entity(tableName = "scheduled_notifications")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class ScheduledNotification(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "type_id")
    var typeId: Int = 0,
    @ColumnInfo(name = "content_id")
    var contentId: Int = 0
)
