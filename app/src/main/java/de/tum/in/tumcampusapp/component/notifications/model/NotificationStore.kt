package de.tum.`in`.tumcampusapp.component.notifications.model

import android.content.Context
import de.tum.`in`.tumcampusapp.component.notifications.persistence.ScheduledNotification
import de.tum.`in`.tumcampusapp.database.TcaDb

class NotificationStore(context: Context) {

    private val dao = TcaDb.getInstance(context).scheduledNotificationsDao()

    fun find(notification: AppNotification): ScheduledNotification? {
        return dao.find(notification.type.id, notification.id)
    }

    fun save(notification: AppNotification): Long {
        val scheduledNotification = notification.toScheduledNotification()
        return dao.insert(scheduledNotification)
    }

    companion object {

        private var INSTANCE: NotificationStore? = null

        @JvmStatic
        fun getInstance(context: Context): NotificationStore {
            if (INSTANCE == null) {
                INSTANCE = NotificationStore(context)
            }

            return INSTANCE!!
        }

    }

}