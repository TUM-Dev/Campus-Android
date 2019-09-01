package de.tum.`in`.tumcampusapp.component.tumui.tutionfees

import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.Tuition
import org.joda.time.DateTime
import org.joda.time.Days

/**
 * This class is responsible for scheduling reminders about remaining tuition fees. It first reminds
 * the user 30 days before the deadline and repeats those reminders more frequently as the deadline
 * comes closer.
 */
object TuitionNotificationScheduler {

    /**
     * Returns the timestamp of when the next reminder about tuition fees should be scheduled. It
     * takes into account the remaining time until the tuition fees deadline.
     *
     * @param tuition The [Tuition] with its deadline
     * @return The timestamp of the next reminder in milliseconds
     */
    fun getNextNotificationTime(tuition: Tuition): DateTime {
        // As the tuition fees deadline comes closer, we show notifications more often to make
        // sure that the user does not miss it. We begin a month before the deadline, as this
        // ensures that the user has enough time to make the transaction.
        val notificationTimes = arrayOf(
                Days.days(30),
                Days.days(14),
                Days.days(7),
                Days.days(4),
                Days.days(3),
                Days.days(2),
                Days.days(1),
                Days.days(0)
        )

        val remainingDays = Days.daysBetween(DateTime.now(), tuition.deadline)

        // Get the next occurring notification time
        val daysBeforeDeadline = notificationTimes
                .filter { it <= remainingDays }
                .sortedDescending()
                .first()
                .days

        // Setting the notification time at 10am seems like a good trade-off between making sure
        // that the notification doesn't get lost in the morning flood of new notifications and
        // giving the user enough time to make the transaction.
        return tuition.deadline
                .minusDays(daysBeforeDeadline)
                .withHourOfDay(10)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
    }
}