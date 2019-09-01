package de.tum.`in`.tumcampusapp.component.notifications.receivers

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.utils.Const

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }

        val notificationId = intent.getIntExtra(Const.KEY_NOTIFICATION_ID, 0)
        val notification = intent.getParcelableExtra<Notification>(Const.KEY_NOTIFICATION) ?: return

        NotificationScheduler.removeActiveAlarm(context, notificationId.toLong())

        NotificationManagerCompat
                .from(context)
                .notify(notificationId, notification)
    }
}