package de.tum.`in`.tumcampusapp.component.other.notifications

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.NextLectureCard
import de.tum.`in`.tumcampusapp.utils.DateUtils

class CalendarNotificationsProvider(context: Context,
                                    private val lectures: List<NextLectureCard.CalendarItem>) : NotificationsProvider(context) {

    override fun getNotifications(): List<AppNotification> {
        val firstLecture = lectures.firstOrNull() ?: return emptyList()
        val time = DateUtils.getFutureTime(firstLecture.start, context)

        notificationBuilder
                .setContentText("${firstLecture.title}\n${time}")
                .setSmallIcon(R.drawable.ic_notification)

        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.wear_next_lecture)
        notificationBuilder
                .extend(NotificationCompat.WearableExtender().setBackground(bitmap))

        val notification = notificationBuilder.build()

        return ArrayList<AppNotification>().apply {
            add(InstantNotification(notification))
        }
    }

}