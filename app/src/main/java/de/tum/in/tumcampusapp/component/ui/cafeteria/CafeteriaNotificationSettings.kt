package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.content.Context
import android.preference.PreferenceManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalTime

class CafeteriaNotificationSettings(context: Context) {

    private val cafeteriaMenuManager = CafeteriaMenuManager(context)
    private val persistentStore = CafeteriaNotificationSettingsStore(context)

    init {
        val didChangeSettings = workWeek
                .map { writeDayToSettings(it, defaultNotificationTime, false) }
                .toBooleanArray()
                .any { it }

        if (didChangeSettings) {
            cafeteriaMenuManager.scheduleFoodAlarms(true)
        }
    }

    private fun writeDayToSettings(day: DateTime, newTime: LocalTime?, overwrite: Boolean): Boolean {
        val storedTime = persistentStore.retrieveTimeForDay(day)
        storedTime?.let {
            return if (overwrite && it != newTime) {
                persistentStore.storeTimeForDay(day, newTime)
                true
            } else {
                false
            }
        }

        persistentStore.storeTimeForDay(day, newTime)
        return true
    }

    fun retrieveLocalTime(weekday: DateTime): LocalTime? {
        return persistentStore.retrieveTimeForDay(weekday)
    }

    fun retrieveLocalTimeOrDefault(weekday: DateTime): LocalTime {
        return persistentStore.retrieveTimeForDay(weekday) ?: defaultNotificationTime
    }

    private fun updateLocalTimeOfDay(time: CafeteriaNotificationTime): Boolean {
        val currentlyStored = persistentStore.retrieveTimeForDay(time.weekday)
        if (currentlyStored == time.time) {
            return false
        }

        return writeDayToSettings(time.weekday, time.time, true)
    }

    fun saveEntireSchedule(times: ArrayList<CafeteriaNotificationTime>): Boolean {
        if (times.size != 5) {
            return false
        }

        val didChangeTime = times
                .map { updateLocalTimeOfDay(it) }
                .toBooleanArray()
                .any { it }

        return if (didChangeTime) {
            cafeteriaMenuManager.scheduleFoodAlarms(true)
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

        private val workWeek: List<DateTime> by lazy {
            (DateTimeConstants.MONDAY until DateTimeConstants.SATURDAY)
                    .map { DateTime.now().withDayOfWeek(it) }
        }

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

    private class CafeteriaNotificationSettingsStore(context: Context) {

        private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        fun storeTimeForDay(day: DateTime, time: LocalTime?) {
            storeTimeForDay(day.dayOfWeek, time)
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
            val hour = sharedPrefs.getInt(PREFIX + dayOfWeek + HOUR_SUFFIX, -1)
            val minute = sharedPrefs.getInt(PREFIX + dayOfWeek + MINUTE_SUFFIX, -1)

            if (hour == -1 || minute == -1) {
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
