package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.content.Context
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.notifications.NotificationsProvider
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.FutureNotification
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateUtils
import de.tum.`in`.tumcampusapp.utils.toJoda

class CalendarNotificationsProvider(context: Context,
                                    private val lectures: List<CalendarItem>) : NotificationsProvider(context) {

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_calendar)
                .setWhen(System.currentTimeMillis())
    }

    override fun getNotifications(): List<AppNotification> {
        val firstItem = lectures.firstOrNull() ?: return emptyList()
        val firstItemStart = DateUtils.getDateTime(firstItem.dtstart)
        val time = DateUtils.getFutureTime(firstItemStart, context)

        val notification = getNotificationBuilder()
                .setContentText("${firstItem.title}\n$time")
                .setSmallIcon(R.drawable.ic_notification)
                .build()

        // Schedule the notification 15 minutes before the lecture
        val notificationTime = firstItemStart
                .toJoda()
                .minusMinutes(15)
                .millis

        return ArrayList<AppNotification>().apply {
            add(FutureNotification(AppNotification.CALENDAR_ID, notification, notificationTime))
        }
    }

}