package de.tum.`in`.tumcampusapp.component.other.notifications

import android.app.PendingIntent
import android.content.Context
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.other.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.Departure
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult

class TransportNotificationsProvider(
        context: Context,
        private val departures: List<Departure>,
        private val station: StationResult) : NotificationsProvider(context) {

    override fun getNotifications(): List<AppNotification> {
        val firstDeparture = departures.firstOrNull() ?: return emptyList()

        val intent = firstDeparture.getIntent(context, station)
        if (intent != null) {
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            notificationBuilder.setContentIntent(pendingIntent)
        }

        val title = context.getString(R.string.mvv)
        val text = "${firstDeparture.symbol} ${firstDeparture.direction} in ${firstDeparture.countDown} min"

        val notification = notificationBuilder
                .setContentTitle(title)
                .setContentText(text)
                .build()

        // TODO: Add intelligent scheduling (last lecture)
        // Otherwise, this does not provide much value to the user

        return ArrayList<AppNotification>().apply {
            add(InstantNotification(AppNotification.TRANSPORT_ID, notification))
        }
    }

}