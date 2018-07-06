package de.tum.`in`.tumcampusapp.api.tumonline.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings
import org.joda.time.DateTime

@Entity
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class TumLock(@PrimaryKey
                   var url: String = "",
                   var error: String = "",
                   var timestamp: DateTime = DateTime(),
                   var lockedFor: Int = 0,
                   var active: Int = 0) {
    companion object {
        fun create(url: String, msg: String, lockTime: Int): TumLock {
            return TumLock(url = url, error = msg, timestamp = DateTime.now(), lockedFor = lockTime, active = 1)
        }
    }
}