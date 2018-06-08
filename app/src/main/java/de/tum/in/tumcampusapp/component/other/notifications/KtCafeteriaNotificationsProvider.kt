package de.tum.`in`.tumcampusapp.component.other.notifications

import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.other.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateUtils

class KtCafeteriaNotificationsProvider(
        context: Context,
        private val cafeteria: CafeteriaWithMenus) : NotificationsProvider(context) {

    private val GROUP_KEY_CAFETERIA = "de.tum.in.tumcampus.CAFETERIA"

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_CAFETERIA)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setGroup(GROUP_KEY_CAFETERIA)
                .setGroupSummary(true)
                .setShowWhen(false)
                .setWhen(System.currentTimeMillis())
    }

    private fun getSecondaryNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_CAFETERIA)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setGroup(GROUP_KEY_CAFETERIA)
                .setShowWhen(false)
    }

    override fun getNotifications(): List<AppNotification> {
        val menus = cafeteria.menus.filter { it.typeShort != "bei" }

        val notifications = menus
                .map { menu ->
                    val title = menu.notificationTitle
                    val text = menu.getNotificationText(context)

                    val notificationBuilder = getSecondaryNotificationBuilder()

                    val intent = cafeteria.getIntent(context)
                    if (intent != null) {
                        val pendingIntent = PendingIntent
                                .getActivity(context, 0, intent, 0)
                        notificationBuilder.setContentIntent(pendingIntent)
                    }

                    notificationBuilder
                            .setContentTitle(title)
                            .setContentText(text)
                            .build()
                }
                .mapIndexed { index, notification ->
                    val menuId = menus[index].id
                    InstantNotification(menuId, notification)
                }
                .toCollection(ArrayList())

        val inboxStyle = NotificationCompat.InboxStyle()
        menus.forEach { menu ->
            inboxStyle.addLine(menu.notificationTitle)
        }

        val title = context.getString(R.string.cafeteria)
        val date = DateUtils.getDate(cafeteria.nextMenuDate)
        val text = DateUtils.getDateString(date)

        // TODO: Pending intent?

        val summaryNotification = getNotificationBuilder()
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(inboxStyle)
                .build()

        // This is the summary notification of all news. While individual notifications have their
        // own IDs (newsItem.id), it is important that this summary notification always uses
        // AppNotification.NEWS_ID in order to work reliably.
        val summaryAppNotification = InstantNotification(AppNotification.CAFETERIA_ID, summaryNotification)
        notifications.add(summaryAppNotification)

        return notifications
    }

}