package de.tum.`in`.tumcampusapp.component.tumui.tutionfees

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.notifications.NotificationProvider
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.utils.Const

class TuitionFeesNotificationProvider(context: Context) : NotificationProvider(context) {

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
                .setShowWhen(false)
                .setColor(notificationColorAccent)
    }

    override fun buildNotification(): AppNotification? {
        val tuitionFeeManager = TuitionFeeManager(context)
        val tuition = tuitionFeeManager.loadTuition(CacheControl.USE_CACHE)

        if (tuition == null || tuition.isPaid) {
            return null
        }

        val title = context.getString(R.string.tuition_fees)
        val text = context.getString(R.string.reregister_todo, tuition.deadline)

        val intent = tuition.getIntent(context)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val notification = getNotificationBuilder()
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .build()

        // We can pass 0 as the notification ID because only one notification at a time
        // will be active
        return InstantNotification(NotificationType.TUITION_FEES, 0, notification)
    }

}