package de.tum.`in`.tumcampusapp.component.ui.alarm.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import org.joda.time.DateTime
import java.io.Serializable

@Entity(tableName = "notification")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class FcmNotification(@PrimaryKey
                           var notification: Int = 0,
                           var type: Int = 0,
                           var location: FcmNotificationLocation = FcmNotificationLocation(),
                           var title: String = "",
                           var description: String = "",
                           var signature: String = "",
                           var created: DateTime = DateTime()) : Serializable {
    companion object {
        private const val serialVersionUID = 8643117662605459731L
    }
}
