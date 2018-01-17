package de.tum.`in`.tumcampusapp.models.gcm

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import de.tum.`in`.tumcampusapp.auxiliary.Utils
import java.io.Serializable
import java.util.*

@Entity(tableName = "notification")
data class GCMNotification(@PrimaryKey
                           var notification: Int = 0,
                           var type: Int = 0,
                           var location: GCMNotificationLocation = GCMNotificationLocation(),
                           var title: String = "",
                           var description: String = "",
                           var signature: String = "",
                           var created: String = Utils.getDateString(Date())) : Serializable {
    companion object {
        private const val serialVersionUID = 8643117662605459731L
    }
}
