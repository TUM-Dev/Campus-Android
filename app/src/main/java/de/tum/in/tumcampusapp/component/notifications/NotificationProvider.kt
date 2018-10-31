package de.tum.`in`.tumcampusapp.component.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.FutureNotification

/**
 * Subclasses of this class provide a list of [AppNotification]s that they want to display. These
 * can either be [InstantNotification]s, which will be displayed instantly or [FutureNotification]s,
 * which will be scheduled for later.
 *
 * Subclasses of this class will be invoked in [ProvidesNotifications]'s getNotifications().
 *
 * @param context The current [Context]
 */
abstract class NotificationProvider(protected val context: Context) {

    protected val notificationColorAccent = ContextCompat.getColor(context, R.color.color_primary)

    /**
     * Returns the [NotificationCompat.Builder] with attributes shared by all of the provider's
     * notifications, such as setAutoCancel() or setGroup()
     *
     * @return A [NotificationCompat.Builder] for this provider
     */
    protected abstract fun getNotificationBuilder(): NotificationCompat.Builder

    /**
     * Returns an [AppNotification] that this provider wants to display, or null if no notification
     * should be displayed.
     *
     * @return An [AppNotification] or null
     */
    abstract fun buildNotification(): AppNotification?

}