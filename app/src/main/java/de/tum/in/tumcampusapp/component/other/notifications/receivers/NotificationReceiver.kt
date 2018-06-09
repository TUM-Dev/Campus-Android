package de.tum.`in`.tumcampusapp.component.other.notifications.receivers

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.utils.Const

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }

        val notificationId = intent.getIntExtra(Const.KEY_NOTIFICATION_ID, 0)
        val notification = intent.getParcelableExtra<Notification>(Const.KEY_NOTIFICATION) ?: return

        // If it's a notification about tuition fees, we hand it over to TuitionNotificationReceiver
        if (notificationId == AppNotification.TUITION_FEES_ID) {
            val tuitionNotificationReceiver = TuitionNotificationReceiver(context)
            tuitionNotificationReceiver.onReceive(notificationId, notification)
            return
        }

        // TODO: WearableExtender

        NotificationManagerCompat
                .from(context)
                .notify(notificationId, notification)
    }

}