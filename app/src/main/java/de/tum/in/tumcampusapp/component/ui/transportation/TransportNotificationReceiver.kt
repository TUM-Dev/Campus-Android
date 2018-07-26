package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager

@Deprecated("Use NotificationAlarmReceiver")
class TransportNotificationReceiver(private val context: Context) {

    fun onReceive(notificationId: Int, notification: Notification) {
        val locationManager = LocationManager(context)
        val stationResult = locationManager.getStation() ?: return

        // TODO: Get station close to current campus

        val text = "Departures at ${stationResult.station}"

        val intent = stationResult.getIntent(context)
        val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        /*
        val notificationBuilder = TransportNotificationsProvider
                .getNotificationBuilder(context)
                .setContentTitle(context.getString(R.string.mvv))
                .setContentText(text)
                .setContentIntent(pendingIntent)

        val inboxStyle = NotificationCompat.InboxStyle()

        TransportController
                .getDeparturesFromExternal(context, stationResult.id)
                .take(10)
                .map { "${it.servingLine} (${it.direction}) in ${it.countDown} min" }
                .forEach { inboxStyle.addLine(it) }

        val newNotification = notificationBuilder
                .setStyle(inboxStyle)
                .build()

        NotificationManagerCompat
                .from(context)
                .notify(notificationId, newNotification)
        */
    }

}