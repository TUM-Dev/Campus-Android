package de.tum.`in`.tumcampusapp.models.dbEntities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import de.tum.`in`.tumcampusapp.auxiliary.Utils
import java.util.*

@Entity
data class TumLock(@PrimaryKey
                   var url: String = "",
                   var error: String = "",
                   var timestamp: String = "",
                   var lockedFor: Int = 0,
                   var active: Int = 0) {
    companion object {
        fun create(url: String, msg: String, lockTime: Int): TumLock {
            val now = Utils.getDateTimeString(Date())
            return TumLock(url = url, error = msg, timestamp = now, lockedFor = lockTime, active = 1)
        }
    }
}