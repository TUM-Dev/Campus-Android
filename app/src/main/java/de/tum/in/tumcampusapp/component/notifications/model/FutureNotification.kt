package de.tum.`in`.tumcampusapp.component.notifications.model

import android.app.Notification
import org.joda.time.DateTime

/**
 * Holds a [Notification] that is scheduled to be displayed later.
 *
 * @param id The identifier of the notification
 * @param notification The [Notification] that will be displayed to the user
 * @param time The timestamp at which the [Notification] should be displayed
 */
class FutureNotification(id: Int,
                         notification: Notification,
                         val time: DateTime) : AppNotification(id, notification)