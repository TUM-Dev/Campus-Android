package de.tum.`in`.tumcampusapp.component.other.notifications

import android.app.Notification
import java.util.*

sealed class AppNotification

data class InstantNotification(val notification: Notification) : AppNotification()

data class FutureNotification(val time: Date, val notification: Notification) : AppNotification()