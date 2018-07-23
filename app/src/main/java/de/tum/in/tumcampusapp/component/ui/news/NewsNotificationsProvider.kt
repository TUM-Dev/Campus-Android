package de.tum.`in`.tumcampusapp.component.ui.news

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.notifications.NotificationsProvider
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const

class NewsNotificationsProvider(context: Context,
                                private val newsItems: List<News>) : NotificationsProvider(context) {

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
                .setGroupSummary(true)
                .setGroup(GROUP_KEY_NEWS)
                .setColor(notificationColorAccent)
    }

    /**
     * Returns the [NotificationCompat.Builder] used for secondary notifications (notifications that
     * are grouped under a summary notification.
     *
     * @return The secondary [NotificationCompat.Builder]
     */
    private fun getSecondaryNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_CAFETERIA)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setGroup(GROUP_KEY_NEWS)
                .setShowWhen(false)
                .setColor(notificationColorAccent)
    }

    override fun getNotifications(): List<AppNotification> {
        val newsSourcesDao = TcaDb
                .getInstance(context)
                .newsSourcesDao()

        val notifications = newsItems
                .map { buildSecondaryNotification(it, newsSourcesDao) }
                .toCollection(ArrayList())

        val summaryTitle = context.getString(R.string.news)
        val summaryText = "${notifications.size} new items"

        val inboxStyle = NotificationCompat.InboxStyle()
        newsItems.forEach { inboxStyle.addLine(it.title) }

        val intent = Intent(context, NewsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val summaryNotification = getNotificationBuilder()
                .setContentTitle(summaryTitle)
                .setContentText(summaryText)
                .setStyle(inboxStyle)
                .setContentIntent(pendingIntent)
                .build()

        // This is the summary notification of all news. While individual notifications have their
        // own IDs (newsItem.id), it is important that this summary notification always uses
        // AppNotification.NEWS_ID in order to work reliably.
        val summaryAppNotification = InstantNotification(AppNotification.NEWS_ID, summaryNotification)
        notifications.add(summaryAppNotification)

        return notifications
    }

    /**
     * Returns an [AppNotification] for the provided [News] item.
     *
     * @param newsItem The [News] item of the notification
     * @param newsSourcesDao The [NewsSourcesDao] used to retrieve information about the news source
     * @return An [AppNotification]
     */
    private fun buildSecondaryNotification(newsItem: News,
                                           newsSourcesDao: NewsSourcesDao): InstantNotification {
        val newsSource = newsSourcesDao.getNewsSource(newsItem.src.toInt())
        val notificationBuilder = getSecondaryNotificationBuilder()
                .setContentTitle(newsSource.title)
                .setContentText(newsItem.title)
                .setTicker(newsItem.title)

        val intent = newsItem.getIntent(context)
        if (intent != null) {
            val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            notificationBuilder.setContentIntent(pendingIntent)
        }

        val notification = notificationBuilder.build()
        return InstantNotification(newsItem.id.toInt() , notification)
    }

    companion object {
        private const val GROUP_KEY_NEWS = "de.tum.in.tumcampus.NEWS"
    }

}