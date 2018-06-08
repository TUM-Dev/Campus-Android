package de.tum.`in`.tumcampusapp.component.other.notifications.receivers

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.other.notifications.schedulers.NotificationScheduler

class NotificationReceiver : BroadcastReceiver() {

    // TODO: Register in Manifest

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            return
        }

        val notificationId = intent?.getIntExtra(NotificationScheduler.KEY_NOTIFICATION_ID, 0) ?: 0
        val notification = intent?.getParcelableExtra<Notification>(NotificationScheduler.KEY_NOTIFICATION) ?: return

        // If it's a notification about tuition fees, we hand it over to TuitionNotificationReceiver
        if (notificationId == AppNotification.TUITION_FEES_ID) {
            val tuitionNotificationReceiver = TuitionNotificationReceiver(context)
            tuitionNotificationReceiver.onReceive(notificationId, notification)
            return
        }

        NotificationManagerCompat
                .from(context)
                .notify(notificationId, notification)
    }

}