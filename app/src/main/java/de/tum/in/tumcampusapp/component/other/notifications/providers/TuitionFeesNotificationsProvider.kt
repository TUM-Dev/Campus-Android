package de.tum.`in`.tumcampusapp.component.other.notifications.providers

import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.other.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.Tuition
import de.tum.`in`.tumcampusapp.utils.Const
import java.util.*

class TuitionFeesNotificationsProvider(
        context: Context, private val tuition: Tuition) : NotificationsProvider(context) {

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
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

        // TODO: If deadline is close, turn it red

        val notification = notificationBuilder.build()

        // TODO: Intelligently schedule notifications for 1 month, 2 weeks, 1 week, 3 days
        val futureNotification = InstantNotification(AppNotification.TUITION_FEES_ID, notification)

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