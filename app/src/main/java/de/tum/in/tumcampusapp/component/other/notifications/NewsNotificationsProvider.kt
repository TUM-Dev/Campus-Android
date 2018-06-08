package de.tum.`in`.tumcampusapp.component.other.notifications

import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.other.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const

class NewsNotificationsProvider(context: Context,
                                private val newsItems: List<News>) : NotificationsProvider(context) {

    private val GROUP_KEY_NEWS = "de.tum.in.tumcampus.NEWS"

    override fun getNotifications(): List<AppNotification> {
        val newsSourcesDao = TcaDb
                .getInstance(context)
                .newsSourcesDao()

        val notifications = newsItems
                .map { newsItem ->
                    val intent = newsItem.getIntent(context)
                    if (intent != null) {
                        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                        notificationBuilder.setContentIntent(pendingIntent)
                    }

                    val newsSource = newsSourcesDao.getNewsSource(newsItem.src.toInt())
                    notificationBuilder
                            .setContentTitle(newsSource.title)
                            .setContentText(newsItem.title)
                            .setTicker(newsItem.title)
                            .setGroup(GROUP_KEY_NEWS)

                    val notification = notificationBuilder.build()
                    InstantNotification(newsItem.id.toInt() /*AppNotification.NEWS_ID*/, notification)
                }
                .toCollection(ArrayList())

        val summaryTitle = context.getString(R.string.news)
        val summaryText = "${notifications.size} new items"

        val inboxStyle = NotificationCompat.InboxStyle()
        newsItems.forEach { newsItem ->
            inboxStyle.addLine(newsItem.title)
        }

        val summaryNotification = NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setContentTitle(summaryTitle)
                .setContentText(summaryText)
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(inboxStyle)
                .setGroupSummary(true)
                .setGroup(GROUP_KEY_NEWS)
                .build()

        // This is the summary notification of all news. While individual notifications have their
        // own IDs (newsItem.id), it is important that this summary notification always uses
        // AppNotification.NEWS_ID in order to work reliably.
        val summaryAppNotification = InstantNotification(AppNotification.NEWS_ID, summaryNotification)
        notifications.add(summaryAppNotification)

        return notifications
    }

}