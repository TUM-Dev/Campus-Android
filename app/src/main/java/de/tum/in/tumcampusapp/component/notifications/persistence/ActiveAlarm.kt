package de.tum.`in`.tumcampusapp.component.notifications.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings

@Entity(tableName = "active_alarms")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class ActiveAlarm(@PrimaryKey var id: Long = 0)
