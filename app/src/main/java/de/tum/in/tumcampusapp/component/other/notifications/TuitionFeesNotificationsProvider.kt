package de.tum.`in`.tumcampusapp.component.other.notifications

import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.Tuition
import de.tum.`in`.tumcampusapp.utils.Utils
import java.util.*

class TuitionFeesNotificationsProvider(context: Context,
                                       private val tuition: Tuition) : NotificationsProvider(context) {

    override fun getNotifications(): List<AppNotification> {
        val contentText = when (tuition.isReregistered) {
            true -> String.format(context.getString(R.string.reregister_success), tuition.semesterBez)
            false -> {
                val amount = "${tuition.soll} €\n"
                amount + String.format(context.getString(R.string.reregister_todo), tuition.frist)
            }
        }

        notificationBuilder
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(Utils.getLargeIcon(context, R.drawable.ic_money))

        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.wear_tuition_fee)
        notificationBuilder.extend(NotificationCompat.WearableExtender().setBackground(bitmap))
        val notification = notificationBuilder.build()

        // TODO: Schedule notifications for 1 month, 2 weeks, 1 week, 3 days
        val futureNotification = InstantNotification(notification)

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