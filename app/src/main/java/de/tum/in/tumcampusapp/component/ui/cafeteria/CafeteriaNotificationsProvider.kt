package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.notifications.NotificationsProvider
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.FutureNotification
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.MenuType
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime

class CafeteriaNotificationsProvider(
        context: Context,
        private val cafeteria: CafeteriaWithMenus) : NotificationsProvider(context) {

    private val notificationsStore = CafeteriaNotificationsStore(context)

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_CAFETERIA)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_cutlery)
                .setGroup(GROUP_KEY_CAFETERIA)
                .setGroupSummary(true)
                .setShowWhen(false)
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
                .setSmallIcon(R.drawable.ic_cutlery)
                .setGroup(GROUP_KEY_CAFETERIA)
                .setShowWhen(false)
                .setColor(notificationColorAccent)
    }

    override fun getNotifications(): List<AppNotification> {
        val menus = cafeteria.menus.filter { it.menuType != MenuType.SIDE_DISH }
        val intent = cafeteria.getIntent(context)

        val notificationTime = cafeteria.notificationTime

        // Cancel any cafeteria notifications that have not been cleared by the user
        notificationsStore.clearAll()

        val notifications = menus
                .map { menu -> buildSecondaryNotification(menu, notificationTime, intent) }
                .toCollection(ArrayList())

        val inboxStyle = NotificationCompat.InboxStyle()
        menus.forEach { inboxStyle.addLine(it.notificationTitle) }

        val title = context.getString(R.string.cafeteria)
        val text = DateTimeUtils.getDateString(cafeteria.nextMenuDate)

        val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val summaryNotification = getNotificationBuilder()
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(inboxStyle)
                .setContentIntent(pendingIntent)
                .build()

        // This is the summary notification of all news. While individual notifications have their
        // own IDs (newsItem.id), it is important that this summary notification always uses
        // AppNotification.NEWS_ID in order to work reliably.
        val summaryAppNotification = FutureNotification(
                AppNotification.CAFETERIA_ID, summaryNotification, notificationTime)
        notifications.add(summaryAppNotification)

        // Store all new notifications so that they can be cleared if the user does not remove them
        // before the next cafeteria notification.
        notificationsStore.store(notifications)

        return notifications
    }

    /**
     * Returns an [AppNotification] for the provided [CafeteriaMenu]. Potentially includes an
     * [Intent] to open the [CafeteriaActivity].
     *
     * @param menu The [CafeteriaMenu] for which to build the notification
     * @param notificationTime The time at which the notification should appear
     * @param intent The [Intent] of the notification
     * @return An [AppNotification]
     */
    private fun buildSecondaryNotification(
            menu: CafeteriaMenu, notificationTime: DateTime, intent: Intent?): AppNotification {
        val title = menu.notificationTitle
        val text = menu.getNotificationText(context)

        val notificationBuilder = getSecondaryNotificationBuilder()

        if (intent != null) {
            val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            notificationBuilder.setContentIntent(pendingIntent)
        }

        val inboxStyle = NotificationCompat.InboxStyle()
        val expandedLines = menu.getNotificationLines(context)
        expandedLines.forEach { inboxStyle.addLine(it) }

        val notification = notificationBuilder
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(inboxStyle)
                .setTimeoutAfter(cafeteria.notificationDuration)
                .build()

        return FutureNotification(menu.id, notification, notificationTime)
    }

    /**
     * This class handles the persistence of cafeteria notifications. We use this to cancel all old
     * cafeteria notifications before scheduling new cafeteria notifications.
     *
     * @param context The current [Context]
     */
    private class CafeteriaNotificationsStore(private val context: Context) {

        private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        /**
         * Removes all displayed or scheduled cafeteria notifications.
         */
        fun clearAll() {
            // Cancel each notification individually
            sharedPrefs
                    .getStringSet(KEY_NOTIFICATIONS, emptySet())
                    .map { it.toInt() }
                    .forEach { id ->
                        NotificationManagerCompat
                                .from(context)
                                .cancel(id)
                    }

            // Clear the persistent store of notifications
            sharedPrefs.edit().remove(KEY_NOTIFICATIONS).apply()
        }

        /**
         * Stores all cafeteria notifications that will be displayed.
         *
         * @param notifications The list of [AppNotification]s that will be displayed
         */
        fun store(notifications: List<AppNotification>) {
            val ids = notifications
                    .map { it.id.toString() }
                    .toSet()

            sharedPrefs
                    .edit()
                    .putStringSet(KEY_NOTIFICATIONS, ids)
                    .apply()
        }

    }

    companion object {
        private const val GROUP_KEY_CAFETERIA = "de.tum.in.tumcampus.CAFETERIA"
        private const val KEY_NOTIFICATIONS = "cafeteriaNotifications"
    }

}