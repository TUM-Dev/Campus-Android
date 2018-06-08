package de.tum.`in`.tumcampusapp.component.other.notifications

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            return
        }

        val notificationId = intent?.getIntExtra(NotificationScheduler.KEY_NOTIFICATION_ID, 0) ?: 0
        val notification = intent?.getParcelableExtra<Notification>(NotificationScheduler.KEY_NOTIFICATION)

        if (notification == null) {
            return
        }

        NotificationManagerCompat
                .from(context)
                .notify(notificationId, notification)
    }

}