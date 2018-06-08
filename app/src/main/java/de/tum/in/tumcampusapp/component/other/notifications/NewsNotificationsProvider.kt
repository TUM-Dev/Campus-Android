package de.tum.`in`.tumcampusapp.component.other.notifications

import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.database.TcaDb
import java.io.IOException

class NewsNotificationsProvider(context: Context,
                                private val newsItems: List<News>) : NotificationsProvider(context) {

    override fun getNotifications(): List<AppNotification> {
        val newsSourcesDao = TcaDb
                .getInstance(context)
                .newsSourcesDao()

        return newsItems.map { newsItem ->
            notificationBuilder.setContentTitle(newsItem.title)
            val intent = newsItem.getIntent(context)
            if (intent != null) {
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                notificationBuilder.setContentIntent(pendingIntent)
            }

            val newsSource = newsSourcesDao.getNewsSource(newsItem.src.toInt())
            notificationBuilder
                    .setContentTitle("News")
                    .setContentText(newsItem.title)
                    .setContentInfo(newsSource.title)
                    .setTicker(newsItem.title)
                    .setSmallIcon(R.drawable.ic_notification)

            try {
                if (newsItem.image.isNotEmpty()) {
                    val bitmap = Picasso
                            .get()
                            .load(newsItem.image)
                            .get()
                    notificationBuilder.extend(
                            NotificationCompat.WearableExtender().setBackground(bitmap))
                }
            } catch (e: IOException) {}

            val notification = notificationBuilder.build()
            InstantNotification(notification)
        }
    }

}