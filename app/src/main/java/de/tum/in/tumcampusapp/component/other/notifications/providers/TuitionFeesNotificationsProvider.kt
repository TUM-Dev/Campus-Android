package de.tum.`in`.tumcampusapp.component.other.notifications.providers

import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.other.notifications.model.FutureNotification
import de.tum.`in`.tumcampusapp.component.other.notifications.schedulers.TuitionNotificationScheduler
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.Tuition
import de.tum.`in`.tumcampusapp.utils.Const
import java.util.*

class TuitionFeesNotificationsProvider(
        context: Context, private val tuition: Tuition) : NotificationsProvider(context) {

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
                .setShowWhen(false)
    }

    override fun getNotifications(): List<AppNotification> {
        if (tuition.isReregistered) {
            return emptyList()
        }

        val title = context.getString(R.string.tuition_fees)
        val text = String.format(context.getString(R.string.reregister_todo), tuition.frist)

        val notificationBuilder = getNotificationBuilder()
                .setContentTitle(title)
                .setContentText(text)

        val buffer = TuitionNotificationScheduler.getDaysBuffer(tuition)
        if (buffer.days <= 4) {
            // The deadline is less than a week away
            notificationBuilder.setColorized(true)
            notificationBuilder.color = Color.RED  // TODO: Test color
        }

        val notification = notificationBuilder.build()

        // Schedule notification based on the remaining buffer until the deadline
        val notificationTime = TuitionNotificationScheduler.getNextNotificationTime(tuition)
        val futureNotification = FutureNotification(
                AppNotification.TUITION_FEES_ID, notification, notificationTime)

        val intent = tuition.getIntent(context)
        if (intent != null) {
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            notificationBuilder.setContentIntent(pendingIntent)
        }

        return ArrayList<AppNotification>().apply {
            add(futureNotification)
        }
    }

}