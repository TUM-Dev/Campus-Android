package de.tum.`in`.tumcampusapp.component.other.notifications.providers

import android.content.Context
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification

abstract class NotificationsProvider(protected val context: Context) {

    protected abstract fun getNotificationBuilder(): NotificationCompat.Builder

    abstract fun getNotifications(): List<AppNotification>

}