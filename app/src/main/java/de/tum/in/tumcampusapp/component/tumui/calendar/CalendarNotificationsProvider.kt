package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.content.Context
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.notifications.NotificationsProvider
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.FutureNotification
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils

class CalendarNotificationsProvider(context: Context,
                                    private val lectures: List<CalendarItem>) : NotificationsProvider(context) {

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_calendar)
                .setShowWhen(false)
                .setColor(notificationColorAccent)
    }

    override fun getNotifications(): List<AppNotification> {
        val firstItem = lectures.firstOrNull() ?: return emptyList()
        val firstItemStart = firstItem.eventStart
        val firstItemEnd = firstItem.eventEnd
        val time = DateTimeUtils.formatFutureTime(firstItemStart, context)

        // Schedule the notification 15 minutes before the lecture
        val notificationTime = firstItemStart.minusMinutes(TIME_BEFORE_LECTURE).millis

        // Automatically remove the notification after the lecture finished
        val notificationEnd = firstItemEnd.millis

        val notification = getNotificationBuilder()
                .setContentText("${firstItem.title}\n$time")
                .setTimeoutAfter(notificationEnd - notificationTime)
                .build()

        return ArrayList<AppNotification>().apply {
            add(FutureNotification(AppNotification.CALENDAR_ID, notification, notificationTime))
        }
    }

    companion object {
        private const val TIME_BEFORE_LECTURE = 15 // minutes
    }

}