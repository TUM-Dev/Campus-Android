package de.tum.`in`.tumcampusapp.component.tumui.tutionfees

import android.app.Notification
import android.content.Context
import android.support.v4.app.NotificationManagerCompat
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl

class TuitionNotificationReceiver(private val context: Context) {

    fun onReceive(notificationId: Int, notification: Notification) {
        // Abort if the tuition has been paid
        // TODO: Async
        val tuition = TuitionFeeManager(context).loadTuition(CacheControl.USE_CACHE) ?: return
        if (tuition.isPaid) {
            return
        }

        NotificationManagerCompat
                .from(context)
                .notify(notificationId, notification)
    }

}
