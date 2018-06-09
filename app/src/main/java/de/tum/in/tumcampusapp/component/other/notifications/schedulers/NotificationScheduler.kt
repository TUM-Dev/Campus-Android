package de.tum.`in`.tumcampusapp.component.other.notifications.schedulers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.component.other.notifications.model.FutureNotification
import de.tum.`in`.tumcampusapp.component.other.notifications.receivers.NotificationReceiver
import de.tum.`in`.tumcampusapp.utils.Const

object NotificationScheduler {

    fun schedule(context: Context, futureNotifications: List<FutureNotification>) {
        futureNotifications.forEach { schedule(context, it) }
    }

    fun schedule(context: Context, futureNotification: FutureNotification) {
        val alarmIntent = getAlarmIntent(context, futureNotification)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, futureNotification.time, alarmIntent)
    }

    private fun getAlarmIntent(context: Context, futureNotification: FutureNotification): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(Const.KEY_NOTIFICATION_ID, futureNotification.id)
            putExtra(Const.KEY_NOTIFICATION, futureNotification.notification)
        }
        return PendingIntent.getBroadcast(context,
                futureNotification.id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

}