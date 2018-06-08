package de.tum.`in`.tumcampusapp.component.other.notifications.providers

import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.other.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.Departure
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import de.tum.`in`.tumcampusapp.utils.Const

class TransportNotificationsProvider(
        context: Context,
        private val departures: List<Departure>,
        private val station: StationResult) : NotificationsProvider(context) {

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_MVV)
                .setSmallIcon(R.drawable.ic_notification)
    }

    override fun getNotifications(): List<AppNotification> {
        val firstDeparture = departures.firstOrNull() ?: return emptyList()

        val title = context.getString(R.string.mvv)
        val text = "${firstDeparture.symbol} ${firstDeparture.direction} in ${firstDeparture.countDown} min"

        val notificationBuilder = getNotificationBuilder()
                .setContentTitle(title)
                .setContentText(text)

        val intent = firstDeparture.getIntent(context, station)
        if (intent != null) {
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            notificationBuilder.setContentIntent(pendingIntent)
        }

        // TODO: Add intelligent scheduling (last lecture)
        // Otherwise, this does not provide much value to the user

        val notification = notificationBuilder.build()

        return ArrayList<AppNotification>().apply {
            add(InstantNotification(AppNotification.TRANSPORT_ID, notification))
        }
    }

}