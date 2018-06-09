package de.tum.`in`.tumcampusapp.component.other.notifications.model

import android.app.Notification

/**
 * Holds a [Notification] that is scheduled to be displayed instantly.
 *
 * @param id The identifier of the notification
 * @param notification The [Notification] that will be displayed to the user
 */
class InstantNotification(id: Int, notification: Notification) : AppNotification(id, notification)