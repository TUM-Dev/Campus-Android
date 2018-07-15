package de.tum.`in`.tumcampusapp.component.tumui.tutionfees

import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.Tuition
import de.tum.`in`.tumcampusapp.utils.toJoda
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*

object TuitionNotificationScheduler {

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

        // The buffer between the current date and the deadline
        val daysDiff = getDaysDiff(deadline)

        // Sort possible notification times descending and remove all bigger than the buffer
        // Then, select the biggest number of days until the deadline
        val daysBeforeDeadline = notificationTimes
                .filter { time -> time <= daysDiff }
                .sortedDescending()
                .first()
                .days

        // The date of the notification
        // Set the time to be 10am
        val notificationDate = deadline
                .minusDays(daysBeforeDeadline)
                .withHourOfDay(10)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)

        return notificationDate.millis
    }

    private fun getDaysDiff(deadline: DateTime) = Days.daysBetween(Date().toJoda(), deadline)

}