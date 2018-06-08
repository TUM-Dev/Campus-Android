package de.tum.`in`.tumcampusapp.component.other.notifications

import android.content.Context
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.other.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.utils.DateUtils

class CalendarNotificationsProvider(context: Context,
                                    private val lectures: List<CalendarItem>) : NotificationsProvider(context) {

    override fun getNotifications(): List<AppNotification> {
        val firstItem = lectures.firstOrNull() ?: return emptyList()
        val firstItemStart = DateUtils.getDateTime(firstItem.dtstart)
        val time = DateUtils.getFutureTime(firstItemStart, context)

        val notification = notificationBuilder
                .setContentText("${firstItem.title}\n${time}")
                .setSmallIcon(R.drawable.ic_notification)
                .build()

        return ArrayList<AppNotification>().apply {
            add(InstantNotification(AppNotification.CALENDAR_ID, notification))
        }
    }

}