package de.tum.`in`.tumcampusapp.component.tumui.tutionfees

import android.app.Notification
import android.content.Context
import de.tum.`in`.tumcampusapp.component.notifications.model.FutureNotification
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler

class TuitionNotificationReceiver(private val context: Context) {

    fun onReceive(notificationId: Int, notification: Notification) {
        // Abort if the tuition has been paid
        val tuition = TuitionFeeManager(context).loadTuition() ?: return
        if (tuition.isReregistered) {
            return
        }

        // Schedule the next notification
        val notificationTime = TuitionNotificationScheduler.getNextNotificationTime(tuition)
        val futureNotification = FutureNotification(notificationId, notification, notificationTime)
        NotificationScheduler.schedule(context, futureNotification)
    }

}