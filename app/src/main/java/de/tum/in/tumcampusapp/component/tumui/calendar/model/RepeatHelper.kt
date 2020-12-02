package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import org.joda.time.DateTime
import java.util.*

/**
 * Helper class holding information and providing helpers on repeating events.
 */
data class RepeatHelper(
    var repetitionType: RepetitionType = RepetitionType.NotRepeating,
    var end: DateTime? = null,
    var times: Int = 0,
    var seriesId: String? = UUID.randomUUID().toString()
) {

    fun isTooLong(start: DateTime? = null): Boolean {
        if (repetitionType == RepetitionType.NotRepeating) {
            return false
        }
        if (repetitionType == RepetitionType.RepeatsUntil) {
            return start!!.plusMonths(6).isBefore(end)
        }
        return times > 25
    }

    fun isTooShort(start: DateTime? = null): Boolean {
        if (repetitionType == RepetitionType.NotRepeating) {
            return false
        }
        if (repetitionType == RepetitionType.RepeatsUntil) {
            return start!!.plusWeeks(1).isAfter(end)
        }
        return times < 2
    }

    fun setNotRepeating() {
        repetitionType = RepetitionType.NotRepeating
    }

    fun setRepeatingNTimes() {
        repetitionType = RepetitionType.RepeatsNTimes
    }

    fun setRepeatingUntil() {
        repetitionType = RepetitionType.RepeatsUntil
    }

    fun isNotRepeating(): Boolean {
        return repetitionType == RepetitionType.NotRepeating
    }

    fun isRepeatingNTimes(): Boolean {
        return repetitionType == RepetitionType.RepeatsNTimes
    }

    enum class RepetitionType { NotRepeating, RepeatsNTimes, RepeatsUntil }
}
