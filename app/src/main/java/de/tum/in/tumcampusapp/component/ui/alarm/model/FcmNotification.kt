package de.tum.`in`.tumcampusapp.component.ui.alarm.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import de.tum.`in`.tumcampusapp.utils.DateUtils
import java.io.Serializable
import java.util.*

@Entity(tableName = "notification")
data class FcmNotification(@PrimaryKey
                           var notification: Int = 0,
                           var type: Int = 0,
                           var location: FcmNotificationLocation = FcmNotificationLocation(),
                           var title: String = "",
                           var description: String = "",
                           var signature: String = "",
                           var created: String = DateUtils.getDateString(Date())) : Serializable {
    companion object {
        private const val serialVersionUID = 8643117662605459731L
    }
}
