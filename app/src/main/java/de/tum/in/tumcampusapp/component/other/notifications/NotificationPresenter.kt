package de.tum.`in`.tumcampusapp.component.other.notifications

import android.content.Context
import android.support.v4.app.NotificationManagerCompat
import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification

object NotificationPresenter {

    fun show(context: Context, appNotification: AppNotification) {
        NotificationManagerCompat
                .from(context)
                .notify(appNotification.id, appNotification.notification)
    }

}