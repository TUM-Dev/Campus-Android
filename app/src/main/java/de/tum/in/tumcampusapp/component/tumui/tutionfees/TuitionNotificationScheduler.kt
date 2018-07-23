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
    fun getNextNotificationTime(tuition: Tuition): Long {
        val deadline = tuition.dueDate
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

        // The remaining days between the current date and the deadline
        val remainingDays = Days.daysBetween(DateTime.now(), deadline)

        // Sort possible notification times descending and remove all bigger than the buffer
        // Then, select the biggest number of days until the deadline
        val daysBeforeDeadline = notificationTimes
                .filter { it <= remainingDays }
                .sortedDescending()
                .first()
                .days

        // Set the time of the notification to 10am
        val notificationDate = deadline
                .minusDays(daysBeforeDeadline)
                .withHourOfDay(10)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)

        return notificationDate.millis
    }

}