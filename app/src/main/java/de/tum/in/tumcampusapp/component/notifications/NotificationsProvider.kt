package de.tum.`in`.tumcampusapp.component.notifications

import android.content.Context
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification

/**
 * Subclasses of this class provide a list of [AppNotification]s that they want to display. These
 * can either be [InstantNotification]s, which will be displayed instantly or [FutureNotification]s,
 * which will be scheduled for later.
 *
 * Subclasses of this class will be invoked in [ProvidesNotifications]'s getNotifications().
 *
 * @param context The current [Context]
 */
abstract class NotificationsProvider(protected val context: Context) {

    /**
     * Returns the [NotificationCompat.Builder] with attributes shared by all of the provider's
     * notifications, such as setAutoCancel() or setGroup()
     *
     * @return A [NotificationCompat.Builder] for this provider
     */
    protected abstract fun getNotificationBuilder(): NotificationCompat.Builder

    /**
     * Returns the list of [AppNotification]s that this provider wants to display.
     *
     * @return List of [AppNotification]s
     */
    abstract fun getNotifications(): List<AppNotification>

}