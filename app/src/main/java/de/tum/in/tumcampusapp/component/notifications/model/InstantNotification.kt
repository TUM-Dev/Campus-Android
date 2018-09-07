package de.tum.`in`.tumcampusapp.component.notifications.model

import android.app.Notification
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType

/**
 * Holds a [Notification] that is scheduled to be displayed instantly.
 *
 * @param id The identifier of the notification
 * @param notification The [Notification] that will be displayed to the user
 */
class InstantNotification(
        type: NotificationType,
        id: Int,
        notification: Notification
) : AppNotification(type, id, notification)