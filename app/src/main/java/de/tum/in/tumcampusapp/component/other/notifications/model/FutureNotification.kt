package de.tum.`in`.tumcampusapp.component.other.notifications.model

import android.app.Notification

class FutureNotification(id: Int,
                         notification: Notification,
                         val time: Long) : AppNotification(id, notification)