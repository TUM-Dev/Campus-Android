package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.text.format.DateUtils.*
import de.tum.`in`.tumcampusapp.R
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.text.ParseException
import java.util.*

object DateTimeUtils {
    /**
     * Format an upcoming string nicely by being more precise as time comes closer
     */
    fun getFutureTime(time: DateTime, context: Context): String {
        val timeInMillis = time.millis
        val now = Calendar.getInstance().timeInMillis

        //Catch future dates: current clock might be running behind
        if (timeInMillis < now || timeInMillis <= 0) {
            return DateTimeUtils.getTimeOrDay(time, context)
        }

        val diff = timeInMillis - now
        return when {
            diff < 60 * MINUTE_IN_MILLIS -> {
                val formatter = DateTimeFormat.forPattern("m")
                        .withLocale(Locale.ENGLISH)
                context.getString(R.string.IN) + ' '.toString() + formatter.print(DateTime(diff)) + ' '.toString() + context.getString(R.string.MINUTES)
            }
            diff < 3 * HOUR_IN_MILLIS -> { // Be more precise by telling the user the exact time if below 3 hours
                val formatter = DateTimeFormat.forPattern("HH:mm")
                        .withLocale(Locale.ENGLISH)
                context.getString(R.string.AT) + ' '.toString() + formatter.print(time)
            }
            else -> getRelativeTimeSpanString(timeInMillis, now, android.text.format.DateUtils.MINUTE_IN_MILLIS, android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE)
                    .toString()
        }
    }

    /**
     * Format a past timestamp with degrading granularity
     */
    fun getTimeOrDayISO(datetime: String, context: Context): String {
        val d = DateTimeUtils.parseIsoDate(datetime) ?: return ""
        return DateTimeUtils.getTimeOrDay(d, context)
    }

    fun getTimeOrDay(time: DateTime, context: Context): String {
        val timeInMillis = time.millis
        val now = DateTime.now().millis

        // Catch future dates: current clock might be running behind
        if (timeInMillis > now || timeInMillis <= 0) {
            return context.getString(R.string.just_now)
        }

        val diff = now - timeInMillis
        return when {
            diff < MINUTE_IN_MILLIS ->
                context.getString(R.string.just_now)
            diff < 24 * HOUR_IN_MILLIS ->
                DateTimeFormat.forPattern("HH:mm")
                        .withLocale(Locale.ENGLISH)
                        .print(time)
            diff < 48 * HOUR_IN_MILLIS ->
                context.getString(R.string.yesterday)
            else ->
                DateTimeFormat.forPattern("dd.MM.yyyy")
                        .withLocale(Locale.ENGLISH)
                        .print(time)
        }
    }

    // 2014-06-30T16:31:57Z
    private val isoDateFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    fun parseIsoDate(datetime: String) = try {
        isoDateFormat.parseDateTime(datetime)
    } catch (e: ParseException) {
        Utils.log(e)
        null
    }

    // 2014-06-30T16:31:57.878Z
    private val isoDateWithMillisFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    fun parseIsoDateWithMillis(datetime: String) = try {
        isoDateWithMillisFormat.parseDateTime(datetime)
    } catch (e: ParseException) {
        Utils.log(e)
        null
    }

    /**
     * Checks whether two DateTime contain the same day
     *
     * @return true if both dates are on the same day
     */
    fun isSameDay(first: DateTime, second: DateTime) =
            first.year() == second.year() && first.dayOfYear() == second.dayOfYear()


    private val dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    /**
     * Converts a date-string to DateTime
     *
     * @param str String with ISO-Date (yyyy-mm-dd)
     * @return DateTime
     */
    fun getDate(str: String): DateTime = try {
        DateTime.parse(str, dateFormat)
    } catch (e: RuntimeException) {
        Utils.log(e)
        DateTime()
    }

    /**
     * Converts DateTime to an ISO date-string
     *
     * @param d DateTime
     * @return String (yyyy-mm-dd)
     */
    fun getDateString(d: DateTime): String = dateFormat.print(d)

    private val dateTimeFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Converts DateTime to an ISO datetime-string
     *
     * @return String (yyyy-mm-dd hh:mm:ss)
     */
    fun getDateTimeString(d: DateTime): String = dateTimeFormat.print(d)

    /**
     * Converts a datetime-string to DateTime
     *
     * @param str String with ISO-DateTime (yyyy-mm-dd hh:mm:ss)
     */
    fun getDateTime(str: String): DateTime = try {
        DateTime.parse(str, dateTimeFormat)
    } catch (e: RuntimeException) {
        Utils.log(e)
        DateTime()
    }
}
