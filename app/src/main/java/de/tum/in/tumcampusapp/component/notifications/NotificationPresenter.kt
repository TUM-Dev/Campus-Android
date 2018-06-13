package de.tum.`in`.tumcampusapp.component.notifications

import android.content.Context
import android.support.v4.app.NotificationManagerCompat
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.InstantNotification

object NotificationPresenter {

    fun show(context: Context, appNotifications: List<InstantNotification>) {
        appNotifications.forEach { show(context, it) }
    }

    fun show(context: Context, appNotification: AppNotification) {
        NotificationManagerCompat
                .from(context)
                .notify(appNotification.id, appNotification.notification)
    }

}