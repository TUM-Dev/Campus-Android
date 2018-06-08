package de.tum.`in`.tumcampusapp.component.other.notifications

import android.content.Context
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.utils.Const

abstract class NotificationsProvider(protected val context: Context) {

    val notificationBuilder = NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_notification)

    abstract fun getNotifications(): List<AppNotification>

}