package de.tum.`in`.tumcampusapp.component.ui.news

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.notifications.NotificationProvider
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.utils.Const

class NewsNotificationProvider(
    context: Context,
    private val newsItems: List<News>
) : NotificationProvider(context) {

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
                .setGroupSummary(true)
                .setGroup(GROUP_KEY_NEWS)
                .setColor(notificationColorAccent)
    }

    override fun buildNotification(): AppNotification? {
        val summaryTitle = context.getString(R.string.news)
        val summaryText = context.getString(R.string.new_items_format_string, newsItems.size)

        val inboxStyle = NotificationCompat.InboxStyle()
        newsItems.forEach { inboxStyle.addLine(it.title) }

        val intent = Intent(context, NewsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = getNotificationBuilder()
                .setContentTitle(summaryTitle)
                .setContentText(summaryText)
                .setStyle(inboxStyle)
                .setContentIntent(pendingIntent)
                .build()

        // We can pass 0 as the notification ID because only one notification at a time
        // will be active
        return InstantNotification(NotificationType.NEWS, 0, notification)
    }

    companion object {
        private const val GROUP_KEY_NEWS = "de.tum.in.tumcampus.NEWS"
    }
}