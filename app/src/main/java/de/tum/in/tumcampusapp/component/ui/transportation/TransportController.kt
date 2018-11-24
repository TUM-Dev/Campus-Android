package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.content.Context
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.notifications.ProvidesNotifications
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.Event
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Transport Manager, handles querying data from mvv and card creation
 */
class TransportController(private val context: Context) : ProvidesNotifications {

    // TODO Rename to TransportNotificationsProvider

    override fun hasNotificationsEnabled(): Boolean {
        return Utils.getSettingBool(context, "card_mvv_phone", false)
    }

    fun scheduleNotifications(events: List<Event>) {
        if (events.isEmpty()) {
            return
        }

        // Be responsible when scheduling alarms. We don't want to exceed system resources
        // By only using up half of the remaining resources, we achieve fair distribution of the
        // remaining usable notifications
        val maxNotificationsToSchedule = NotificationScheduler.maxRemainingAlarms(context) / 2

        // Schedule a notification alarm for every last calendar item of a day
        val notificationCandidates = events
                .dropLast(1)
                .filterIndexed { index, current ->
                    val next = events[index + 1]
                    if (current.startTime == null || next.startTime == null) {
                        false
                    } else {
                        current.startTime.dayOfYear != next.startTime.dayOfYear
                    }
                }
                .take(maxNotificationsToSchedule) // Some manufacturers cap the amount of alarms you can schedule (https://stackoverflow.com/a/29610474)

        val notifications = notificationCandidates.mapNotNull { it.toNotification(context) }
        NotificationScheduler(context).schedule(notifications)
    }

}
