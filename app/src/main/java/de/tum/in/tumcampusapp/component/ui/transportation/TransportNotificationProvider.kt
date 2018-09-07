package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.notifications.NotificationProvider
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager
import de.tum.`in`.tumcampusapp.utils.Const

class TransportNotificationProvider(context: Context) : NotificationProvider(context) {

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_MVV)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(notificationColorAccent)
    }

    override fun buildNotification(): AppNotification? {
        val locationManager = LocationManager(context)
        val station = locationManager.getStation() ?: return null

        val title = context.getString(R.string.mvv)
        val text = "Departures at ${station.station}"

        val inboxStyle = NotificationCompat.InboxStyle()
        TransportController
                .getDeparturesFromExternal(context, station.id)
                .blockingFirst()
                .map { "${it.servingLine} (${it.direction}) in ${it.countDown} min" }
                .forEach { inboxStyle.addLine(it) }

        val intent = station.getIntent(context)
        val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = getNotificationBuilder()
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(inboxStyle)
                .build()

        return InstantNotification(NotificationType.TRANSPORT, 0, notification)
    }

}
