package de.tum.`in`.tumcampusapp.component.notifications

import android.content.Context
import android.support.v4.app.NotificationManagerCompat
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.NotificationStore

/**
 * This class is responsible for displaying [AppNotification]s to the user.
 */
object NotificationPresenter {

    fun show(context: Context, notification: InstantNotification) {
        val persistentStore = NotificationStore.getInstance(context)
        val globalId = persistentStore.save(notification)
        show(context, globalId.toInt(), notification)
    }

    fun show(context: Context, id: Int, notification: AppNotification) {
        NotificationManagerCompat
                .from(context)
                .notify(id, notification.notification)
    }

}
