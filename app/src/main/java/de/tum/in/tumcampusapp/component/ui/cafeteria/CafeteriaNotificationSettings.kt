package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.content.Context
import android.preference.PreferenceManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import org.joda.time.DateTime
import org.joda.time.LocalTime

/**
 * This class encapsulates the user's settings for cafeteria notifications. It allows retrieval and
 * manipulation of notification times. It also reschedules alarms if notifications times are changed.
 *
 * @param context A [Context]
 */
class CafeteriaNotificationSettings(context: Context) {

    private val cafeteriaMenuManager = CafeteriaMenuManager(context)
    private val persistentStore = CafeteriaNotificationSettingsStore(context)

    /**
     * Returns the [LocalTime] of cafeteria notifications on a particular weekday, or null if the
     * user disabled notifications on this weekday.
     *
     * @param weekday The [DateTime] representing the weekday
     * @return The [LocalTime] of the notification or null
     */
    fun retrieveLocalTime(weekday: DateTime): LocalTime? {
        return persistentStore.retrieveTimeForDay(weekday)
    }

    /**
     * Returns the [LocalTime] of cafeteria notifications on a particular weekday. If the user
     * disabled notifications on this weekday, this method returns the default notification time.
     *
     * @param weekday The [DateTime] representing the weekday
     * @return The [LocalTime] of the notification or the default notification time
     */
    fun retrieveLocalTimeOrDefault(weekday: DateTime): LocalTime {
        return retrieveLocalTime(weekday) ?: defaultNotificationTime
    }

    /**
     * Stores the provided [CafeteriaNotificationTime].
     *
     * @param notificationTime The [CafeteriaNotificationTime] to store
     * @return Boolean whether the data in [CafeteriaNotificationSettingsStore] was updated
     */
    private fun updateLocalTimeOfDay(notificationTime: CafeteriaNotificationTime): Boolean {
        val currentlyStored = persistentStore.retrieveTimeForDay(notificationTime.weekday)

        if (notificationTime.time == currentlyStored) {
            return false
        }

        persistentStore.storeNotificationTime(notificationTime)
        return true
    }

    /**
     * Stores all [CafeteriaNotificationTime]s in the [CafeteriaNotificationSettingsStore] and
     * returns whether any time was actually changed.
     *
     * @param times The list of [CafeteriaNotificationTime]s
     * @return Boolean whether any time was actually changed
     */
    fun saveEntireSchedule(times: List<CafeteriaNotificationTime>): Boolean {
        if (times.size != 5) {
            return false
        }

        val didChangeTime = times
                .map { updateLocalTimeOfDay(it) }
                .toBooleanArray()
                .any { it }

        return if (didChangeTime) {
            cafeteriaMenuManager.scheduleNotificationAlarms()
            true
        } else {
            false
        }
    }

    companion object {

        private const val PREFIX = "CAFETERIA_SCHEDULE_"
        private const val HOUR_SUFFIX = "_HOUR"
        private const val MINUTE_SUFFIX = "_MINUTE"
        private const val NO_VALUE_SET = -1

        private var INSTANCE: CafeteriaNotificationSettings? = null

        private val defaultNotificationTime: LocalTime by lazy {
            LocalTime.now()
                    .withHourOfDay(9)
                    .withMinuteOfHour(30)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0)
        }

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): CafeteriaNotificationSettings {
            if (INSTANCE == null) {
                INSTANCE = CafeteriaNotificationSettings(context)
            }

            return INSTANCE!!
        }
    }

    /**
     * This class is responsible for storing and retrieving [CafeteriaNotificationTime]s.
     *
     * @param context A [Context]
     */
    private class CafeteriaNotificationSettingsStore(context: Context) {

        private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        fun storeNotificationTime(notificationTime: CafeteriaNotificationTime) {
            storeTimeForDay(notificationTime.weekday.dayOfWeek, notificationTime.time)
        }

        private fun storeTimeForDay(dayOfWeek: Int, time: LocalTime?) {
            val hour = time?.hourOfDay ?: NO_VALUE_SET
            val minute = time?.minuteOfHour ?: NO_VALUE_SET
            sharedPrefs
                    .edit()
                    .putInt(PREFIX + dayOfWeek + HOUR_SUFFIX, hour)
                    .putInt(PREFIX + dayOfWeek + MINUTE_SUFFIX, minute)
                    .apply()
        }

        fun retrieveTimeForDay(day: DateTime) = retrieveTimeForDay(day.dayOfWeek)

        private fun retrieveTimeForDay(dayOfWeek: Int): LocalTime? {
            // The initial version of the app stored the hour and the minute value of the
            // notification time separately. To not break things, we continue to do so, but expose
            // the information as a LocalTime.
            val hour = sharedPrefs.getInt(PREFIX + dayOfWeek + HOUR_SUFFIX, NO_VALUE_SET)
            val minute = sharedPrefs.getInt(PREFIX + dayOfWeek + MINUTE_SUFFIX, NO_VALUE_SET)

            if (hour == NO_VALUE_SET || minute == NO_VALUE_SET) {
                return null
            }

            return LocalTime.now()
                    .withHourOfDay(hour)
                    .withMinuteOfHour(minute)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0)
        }
    }
}
