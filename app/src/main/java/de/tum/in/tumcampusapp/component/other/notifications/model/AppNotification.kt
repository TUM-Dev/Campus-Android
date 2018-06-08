package de.tum.`in`.tumcampusapp.component.other.notifications.model

import android.app.Notification

abstract class AppNotification(val id: Int, val notification: Notification) {

    companion object {

        @JvmField val CAFETERIA_ID = 0
        @JvmField val CALENDAR_ID = 1
        @JvmField val NEWS_ID = 2
        @JvmField val TRANSPORT_ID = 3
        @JvmField val TUITION_FEES_ID = 4

    }

}