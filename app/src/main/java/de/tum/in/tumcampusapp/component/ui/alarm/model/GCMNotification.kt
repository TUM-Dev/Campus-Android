package de.tum.`in`.tumcampusapp.component.ui.alarm.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings
import org.joda.time.DateTime
import java.io.Serializable

@Entity(tableName = "notification")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class GCMNotification(@PrimaryKey
                           var notification: Int = 0,
                           var type: Int = 0,
                           var location: GCMNotificationLocation = GCMNotificationLocation(),
                           var title: String = "",
                           var description: String = "",
                           var signature: String = "",
                           var created: DateTime = DateTime()) : Serializable {
    companion object {
        private const val serialVersionUID = 8643117662605459731L
    }
}
