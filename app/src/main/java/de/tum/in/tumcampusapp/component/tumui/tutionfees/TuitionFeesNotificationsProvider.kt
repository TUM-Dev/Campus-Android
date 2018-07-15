package de.tum.`in`.tumcampusapp.component.tumui.tutionfees

import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.notifications.NotificationsProvider
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.FutureNotification
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.Tuition
import de.tum.`in`.tumcampusapp.utils.Const
import java.text.DateFormat
import java.util.*

class TuitionFeesNotificationsProvider(
        context: Context, private val tuition: Tuition) : NotificationsProvider(context) {

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
                .setShowWhen(false)
                .setColor(notificationColorAccent)
    }

    override fun getNotifications(): List<AppNotification> {
        if (tuition.isReregistered) {
            return emptyList()
        }

        val deadline = tuition.dueDate
        val deadlineText = DateFormat.getDateInstance().format(deadline)

        val title = context.getString(R.string.tuition_fees)
        val text = String.format(context.getString(R.string.reregister_todo), deadlineText)

        val notificationBuilder = getNotificationBuilder()
                .setContentTitle(title)
                .setContentText(text)

        val intent = tuition.getIntent(context)
        if (intent != null) {
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            notificationBuilder.setContentIntent(pendingIntent)
        }

        val notification = notificationBuilder.build()

        // Schedule notification based on the remaining buffer until the deadline
        val notificationTime = TuitionNotificationScheduler.getNextNotificationTime(tuition)
        val futureNotification = FutureNotification(
                AppNotification.TUITION_FEES_ID, notification, notificationTime)

        return ArrayList<AppNotification>().apply {
            add(futureNotification)
        }
    }

}