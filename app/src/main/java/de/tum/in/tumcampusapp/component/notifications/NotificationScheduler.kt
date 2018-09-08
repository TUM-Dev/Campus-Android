package de.tum.`in`.tumcampusapp.component.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.FutureNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.NotificationStore
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.notifications.receivers.NotificationAlarmReceiver
import de.tum.`in`.tumcampusapp.component.notifications.receivers.NotificationReceiver
import de.tum.`in`.tumcampusapp.utils.Const
import org.jetbrains.anko.alarmManager
import org.jetbrains.anko.notificationManager
import org.joda.time.DateTime

/**
 * This class is responsible for scheduling notifications. This can either be a concrete notification
 * or a potential notification, for instance scheduling an alarm 30 minutes before the end of a
 * lecture to check for departures at nearby transit stops.
 *
 * @param context The used [Context]
 */
class NotificationScheduler(private val context: Context) {

    private val notificationManager = context.notificationManager

    /**
     * Schedules a list of [FutureNotification]s for the time specified by each notification.
     *
     * @param futureNotifications The list of [FutureNotification]s to schedule
     */
    fun schedule(futureNotifications: List<FutureNotification>) {
        futureNotifications.forEach { schedule(it) }
    }

    /**
     * Schedules an [AppNotification]. Depending on the concrete class ([InstantNotification] or
     * [FutureNotification]), it either directly displays the notification via [NotificationPresenter]
     * or schedules it for later.
     *
     * @param notification The [AppNotification] to schedule
     */
    fun schedule(notification: AppNotification) {
        when (notification) {
            is FutureNotification -> schedule(notification)
            is InstantNotification -> NotificationPresenter.show(context, notification)
        }
    }

    /**
     * Schedules a [FutureNotification]. To prevent duplicates, it first gets any previous versions
     * of this notification via [NotificationStore] and cancels them. Then, it schedules the new
     * notification.
     *
     * @param notification The [FutureNotification] to schedule
     */
    fun schedule(notification: FutureNotification) {
        val persistentStore = NotificationStore.getInstance(context)
        val scheduledNotification = persistentStore.find(notification)
        scheduledNotification?.let {
            // A notification for the same content has been scheduled before. We cancel the previous
            // notification to prevent duplicates.
            cancel(it.id, notification)
            notificationManager.cancel(it.id.toInt())
        }

        val globalNotificationId = persistentStore.save(notification)
        scheduleAlarm(notification, globalNotificationId)
    }

    /**
     * Cancels a previously scheduled notification alarm. It uses the provided global notification
     * ID and [FutureNotification] to re-create the [PendingIntent] and then uses it to cancel the
     * alarm with the [AlarmManager].
     *
     * @param globalId The global ID of the notification as retrieved from [NotificationStore]
     * @param notification The [FutureNotification] that is to be canceled
     */
    fun cancel(globalId: Long, notification: FutureNotification) {
        val pendingIntent = getAlarmIntent(notification, globalId)
        pendingIntent.cancel()
        context.alarmManager.cancel(pendingIntent)
    }

    /**
     * Schedules alarms for a [NotificationType] at a number of [DateTime]s.
     *
     * @param type The [NotificationType] of the alarm
     * @param times The [DateTime]s at which to alarm the [NotificationAlarmReceiver]
     */
    fun scheduleAlarms(type: NotificationType, times: List<DateTime>) {
        times.forEach { scheduleAlarm(type, it) }
    }

    /**
     * Schedules an alarm for a [NotificationType] at a specific [DateTime]. At the time of the
     * alarm, the [NotificationAlarmReceiver] will invoke the [NotificationProvider] to the
     * [NotificationType] and allow it to display a notification.
     *
     * This is used for scheduling alarm for tuition fees and MVV departures. In case of the latter,
     * we specify an alarm at the end of each lecture. In the TransportNotificationsProvider,
     * we check whether or not this is the last lecture of the day. If so, we load departures at the
     * nearest station and display them to the user.
     *
     * @param type The [NotificationType] of the alarm
     * @param time The [DateTime] at which to alarm the [NotificationAlarmReceiver]
     */
    fun scheduleAlarm(type: NotificationType, time: DateTime) {
        val alarmIntent = getAlarmIntent(type)
        val alarmManager = context.alarmManager

        // If the same alarm has already been scheduled, we cancel it.
        alarmIntent.cancel()
        alarmManager.cancel(alarmIntent)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.millis, alarmIntent)
    }

    /**
     * Schedules a [FutureNotification] with the provided global notification ID.
     *
     * @param notification The [FutureNotification] to schedule
     * @param globalId The notification's global ID in [NotificationStore] used for scheduling
     */
    private fun scheduleAlarm(notification: FutureNotification, globalId: Long) {
        val alarmIntent = getAlarmIntent(notification, globalId)
        val alarmManager = context.alarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, notification.time.millis, alarmIntent)
    }

    /**
     * Returns a [PendingIntent] that contains the [FutureNotification]'s ID and
     * notification content.
     *
     * @param futureNotification The [FutureNotification] to schedule
     * @param globalNotificationId The notification's global ID in [NotificationStore] used for scheduling
     * @return The [PendingIntent] used for scheduling the notification
     */
    private fun getAlarmIntent(futureNotification: FutureNotification,
                               globalNotificationId: Long): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(Const.KEY_NOTIFICATION_ID, globalNotificationId.toInt())
            putExtra(Const.KEY_NOTIFICATION, futureNotification.notification)
        }
        return PendingIntent.getBroadcast(context,
                futureNotification.id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    /**
     * Returns a [PendingIntent] that contains the [NotificationType]'s ID. When the alarm goes off,
     * the [NotificationType]'s associated [NotificationProvider] gets the opportunity to display
     * a notification.
     *
     * @param type The [NotificationType] for which to schedule the alarm
     * @return The [PendingIntent] used for scheduling the notification
     */
    private fun getAlarmIntent(type: NotificationType): PendingIntent {
        val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
            putExtra(Const.KEY_NOTIFICATION_TYPE_ID, type.id)
        }
        return PendingIntent.getBroadcast(context, type.id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

}