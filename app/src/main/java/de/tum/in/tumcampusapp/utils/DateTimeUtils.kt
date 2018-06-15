package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.text.format.DateUtils.getRelativeDateTimeString
import android.text.format.DateUtils.getRelativeTimeSpanString
import de.tum.`in`.tumcampusapp.R
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private const val MINUTE_MILLIS = android.text.format.DateUtils.MINUTE_IN_MILLIS
    private const val HOUR_MILLIS = android.text.format.DateUtils.HOUR_IN_MILLIS
    private const val DAY_MILLIS = android.text.format.DateUtils.DAY_IN_MILLIS

    /*
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
        if (diff < 60 * MINUTE_MILLIS) {
            val formatter = SimpleDateFormat("m", Locale.ENGLISH)
            return context.getString(R.string.IN) + ' '.toString() + formatter.format(Date(diff)) + ' '.toString() + context.getString(R.string.MINUTES)
        } else if (diff < 3 * HOUR_MILLIS) { // Be more precise by telling the user the exact time if below 3 hours
            val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            return context.getString(R.string.AT) + ' '.toString() + formatter.format(time)
        } else {
            return getRelativeTimeSpanString(timeInMillis, now, android.text.format.DateUtils.MINUTE_IN_MILLIS, android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE)
                    .toString()
        }
    }

    private fun getRelativeTime(date: DateTime, context: Context): String {
        return getRelativeDateTimeString(context, date.millis, MINUTE_MILLIS, DAY_MILLIS * 2L, 0)
                .toString()
    }

    /*
     * Format a past timestamp with degrading granularity
     */
    fun getTimeOrDayISO(datetime: String, context: Context): String {
        val d = DateTimeUtils.parseIsoDate(datetime) ?: return ""
        return DateTimeUtils.getTimeOrDay(d, context)
    }

    fun getTimeOrDay(time: DateTime, context: Context): String {
        val timeInMillis = time.millis
        val now = DateTime.now().millis

        //Catch future dates: current clock might be running behind
        if (timeInMillis > now || timeInMillis <= 0) {
            return context.getString(R.string.just_now)
        }

        val diff = now - timeInMillis
        return if (diff < MINUTE_MILLIS) {
            context.getString(R.string.just_now)
        } else if (diff < 24 * HOUR_MILLIS) {
            val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            formatter.format(time)
        } else if (diff < 48 * HOUR_MILLIS) {
            context.getString(R.string.yesterday)
        } else {
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
            formatter.format(time)
        }
    }

    // 2014-06-30T16:31:57Z)
    private val isoDateFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    fun parseIsoDate(datetime: String) = try {
        isoDateFormat.parseDateTime(datetime)
    } catch (e: ParseException) {
        null
    }

    // 2014-06-30T16:31:57.878Z
    private val isoDateWithMillisFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    fun parseIsoDateWithMillis(datetime: String) = try {
        isoDateWithMillisFormat.parseDateTime(datetime)
    } catch (e: ParseException) {
        null
    }

    /**
     * Checks whether two Dates contain the same day
     *
     * @return true if both dates are on the same day
     */
    fun isSameDay(first: DateTime, second: DateTime) =
            first.year() == second.year() && first.dayOfYear() == second.dayOfYear()


    private val dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    /**
     * Converts a date-string to Date
     *
     * @param str String with ISO-Date (yyyy-mm-dd)
     * @return Date
     */
    fun getDate(str: String): DateTime = try {
        DateTime.parse(str, dateFormat)
    } catch (e: RuntimeException) {
        DateTime()
    }

    /**
     * Converts Date to an ISO date-string
     *
     * @param d Date
     * @return String (yyyy-mm-dd)
     */
    fun getDateString(d: DateTime): String = dateFormat.print(d)

    private val dateTimeFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Converts Date to an ISO datetime-string
     *
     * @return String (yyyy-mm-dd hh:mm:ss)
     */
    fun getDateTimeString(d: DateTime): String = dateTimeFormat.print(d)

    /**
     * Converts a datetime-string to Date
     *
     * @param str String with ISO-DateTime (yyyy-mm-dd hh:mm:ss)
     */
    fun getDateTime(str: String): DateTime = try {
        DateTime.parse(str, dateTimeFormat)
    } catch (e: RuntimeException) {
        DateTime()
    }
}
