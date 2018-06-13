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

    private val timeBeforeLectures = 15 // minutes

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_calendar)
                .setShowWhen(false)
    }

    override fun getNotifications(): List<AppNotification> {
        val firstItem = lectures.firstOrNull() ?: return emptyList()
        val firstItemStart = DateUtils.getDateTime(firstItem.dtstart)
        val firstItemEnd = DateUtils.getDateTime(firstItem.dtend)
        val time = DateUtils.getFutureTime(firstItemStart, context)

        // Schedule the notification 15 minutes before the lecture
        val notificationTime = firstItemStart
                .toJoda()
                .minusMinutes(timeBeforeLectures)
                .millis

        // automatically remove the notification after the lecture finished
        val notificationEnd = firstItemEnd
                .toJoda()
                .millis

        val notification = getNotificationBuilder()
                .setContentText("${firstItem.title}\n$time")
                .setTimeoutAfter(notificationEnd - notificationTime)
                .build()


        return ArrayList<AppNotification>().apply {
            add(FutureNotification(AppNotification.CALENDAR_ID, notification, notificationTime))
        }
    }

}