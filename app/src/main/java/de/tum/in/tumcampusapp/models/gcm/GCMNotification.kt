package de.tum.`in`.tumcampusapp.models.gcm

import java.io.Serializable

data class GCMNotification(var notification: Int = 0, var type: Int = 0, var location: GCMNotificationLocation = GCMNotificationLocation(), var title: String = "",
                           var description: String = "", var signature: String = "", var created: String = "") : Serializable {
    companion object {
        private const val serialVersionUID = 8643117662605459731L
    }
}
