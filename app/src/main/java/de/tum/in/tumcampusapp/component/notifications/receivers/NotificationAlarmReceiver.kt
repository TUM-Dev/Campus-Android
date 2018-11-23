package de.tum.`in`.tumcampusapp.component.notifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.App
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesNotificationProvider
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationProvider
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportNotificationProvider
import de.tum.`in`.tumcampusapp.utils.Const
import org.jetbrains.anko.doAsync
import javax.inject.Inject

class NotificationAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var cafeteriaNotificationProvider: CafeteriaNotificationProvider

    @Inject
    lateinit var transportNotificationProvider: TransportNotificationProvider

    @Inject
    lateinit var tuitionFeesNotificationProvider: TuitionFeesNotificationProvider

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as App).appComponent.inject(this)

        val typeId = intent.getLongExtra(Const.KEY_NOTIFICATION_TYPE_ID, 0)
        val type = NotificationType.fromId(typeId)

        val notificationProvider = when (type) {
            NotificationType.CAFETERIA -> cafeteriaNotificationProvider
            NotificationType.TRANSPORT -> transportNotificationProvider
            NotificationType.TUITION_FEES -> tuitionFeesNotificationProvider
            else -> return
        }

        doAsync {
            val notification = notificationProvider.buildNotification()
            notification?.let {
                NotificationScheduler(context).schedule(it)
            }
        }
    }

}
