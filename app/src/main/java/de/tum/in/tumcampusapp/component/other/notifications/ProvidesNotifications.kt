package de.tum.`in`.tumcampusapp.component.other.notifications

import de.tum.`in`.tumcampusapp.component.other.notifications.model.AppNotification

interface ProvidesNotifications {

    fun getNotifications(): List<AppNotification>

}