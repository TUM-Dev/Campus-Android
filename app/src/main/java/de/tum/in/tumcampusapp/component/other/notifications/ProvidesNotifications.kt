package de.tum.`in`.tumcampusapp.component.other.notifications

import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification

/**
 * Defines that its implementor provides [AppNotification]s which will be displayed in the user's
 * notifications.
 */
interface ProvidesNotifications {

    /**
     * Returns the list of [AppNotification]s that should be displayed to the user.
     *
     * @return The list of [AppNotification]s
     */
    fun getNotifications(): List<AppNotification>

}