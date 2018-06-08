package de.tum.`in`.tumcampusapp.component.other.notifications

import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.Departure
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils

class TransportNotificationsProvider(
        context: Context,
        private val departures: List<Departure>,
        private val stationNameId: Pair<String, String>) : NotificationsProvider(context) {

    override fun getNotifications(): List<AppNotification> {
        val firstDeparture = departures.firstOrNull() ?: return emptyList()

        val morePageNotification = NotificationCompat.WearableExtender()

        // Add all departures as notification pages
        departures
                .map { departure ->
                    NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_MVV)
                            .setChannelId(Const.NOTIFICATION_CHANNEL_MVV)
                            .setContentTitle("${departure.countDown} min")
                            .setContentText("${departure} ${departure.direction}")
                            .setSmallIcon(R.drawable.ic_notification)
                            .setLargeIcon(Utils.getLargeIcon(context, R.drawable.ic_mvv))
                            .build()
                }
                .forEach { notificationPage ->
                    morePageNotification.addPage(notificationPage)
                }

        val firstDepartureTitle = "${firstDeparture.countDown} min"
        val firstDepartureContent = "${firstDeparture} ${firstDeparture.direction}"

        notificationBuilder
                .setContentTitle(firstDepartureTitle)
                .setContentText(firstDepartureContent)

        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.wear_mvv)
        morePageNotification.background = bitmap

        val intent = firstDeparture.getIntent(context, stationNameId)
        if (intent != null) {
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            notificationBuilder.setContentIntent(pendingIntent)
        }

        val notification = morePageNotification
                .extend(notificationBuilder)
                .build()

        return ArrayList<AppNotification>().apply {
            add(InstantNotification(notification))
        }
    }

}