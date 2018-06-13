package de.tum.`in`.tumcampusapp.component.notifications.model

import android.app.Notification

/**
 * Holds a [Notification] that is to be displayed to the user. Its subclasses can be used to
 * immediately display notifications via [InstantNotification] or to schedule notifications for
 * later via [FutureNotification].
 *
 * @param id The identifier of the notification
 * @param notification The [Notification] that will be displayed to the user
 */
abstract class AppNotification(val id: Int, val notification: Notification) {

    companion object {

        @JvmField val CAFETERIA_ID = 0
        @JvmField val CALENDAR_ID = 1
        @JvmField val NEWS_ID = 2
        @JvmField val TRANSPORT_ID = 3
        @JvmField val TUITION_FEES_ID = 4

    }

}