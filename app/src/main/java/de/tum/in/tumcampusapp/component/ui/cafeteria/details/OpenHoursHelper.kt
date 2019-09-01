package de.tum.`in`.tumcampusapp.component.ui.cafeteria.details

import android.content.Context
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.openinghour.LocationDao
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class OpenHoursHelper(private val context: Context) {
    private val dao: LocationDao = TcaDb.getInstance(context).locationDao()

    /**
     * Converts the opening hours into more readable format.
     * e.g. Opening in 2 hours.
     * HINT: Currently only works for cafeterias, and institutions
     * that have Mo-Do xx-yy.yy, Fr aa-bb and Mo-Fr xx-yy format
     *
     * @param id Location ID, e.g. 100
     * @param date Relative date
     * @return Readable opening string
     */
    fun getHoursByIdAsString(id: Int, date: DateTime): String {
        val result = dao.getHoursByReferenceId(id) ?: return ""

        // Check which week day we have
        val dayOfWeek = date.dayOfWeek

        // Split up the data string from the database with regex which has the format: "Mo-Do 11-14, Fr 11-13.45" or "Mo-Fr 9-20"

        val isGerman = context.getString(R.string.language) == "de"
        val m: Matcher = if (isGerman) {
            Pattern.compile("([a-z]{2}?)[-]?([a-z]{2}?)? ([0-9]{1,2}(?:[\\:][0-9]{2}?)?)-([0-9]{1,2}(?:[\\:][0-9]{2}?)?)", Pattern.CASE_INSENSITIVE)
                    .matcher(result)
        } else {
            // use three letter shortenings for english: Mon, Tue, Wed, Thu, Fri, Sat, Sun
            Pattern.compile("([a-z]{3}?)[-]?([a-z]{3}?)? ([0-9]{1,2}(?:[\\:][0-9]{2}?)?)-([0-9]{1,2}(?:[\\:][0-9]{2}?)?)", Pattern.CASE_INSENSITIVE)
                    .matcher(result)
        }

        // Capture groups for: Mo-Do 9-21.30
        // #0	Mo-Do 9-21.30
        // #1	Mo
        // #2	Do
        // #3	9
        // #4	21.30

        // Find the first part
        val time = arrayOfNulls<String>(2)
        if (m.find()) {
            // We are currently in Mo-Do/Fr, when this weekday is in that range we have our result or we check if the current range is valid for fridays also
            if (dayOfWeek + 1 <= Calendar.THURSDAY || // +1 because dayOfWeek is zero-based while Calendar.THURSDAY is not
                    m.group(2).equals(if (isGerman) "fr" else "fri", ignoreCase = true)) {
                time[0] = m.group(3)
                time[1] = m.group(4)
            } else {
                // Otherwise we need to move to the next match
                if (m.find()) {
                    // Got a match, data should be in capture groups 3/4
                    time[0] = m.group(3)
                    time[1] = m.group(4)
                } else {
                    // No match found, return
                    return ""
                }
            }
        } else {
            // No match found, return
            return ""
        }

        // Convert time to workable calender objects
        val now = DateTime.now()
        val opens = stringToDateTime(date, time[0] ?: "")
        val closes = stringToDateTime(date, time[1] ?: "")

        if (date.dayOfYear() == now.dayOfYear()) {
            // Check the relativity
            val relativeTo: DateTime
            val relation: Int
            if (opens.isAfter(now)) {
                relation = R.string.opens
                relativeTo = opens
            } else if (closes.isAfter(now)) {
                relation = R.string.closes
                relativeTo = closes
            } else {
                relation = R.string.closed
                relativeTo = closes
            }

            // Get the relative string
            val relativeTime = DateTimeUtils.formatFutureTime(relativeTo, context)
            // Return an assembly
            return context.getString(relation) + " " + relativeTime.substring(0, 1)
                    .toLowerCase(Locale.getDefault()) + relativeTime.substring(1)
        } else {
            // future --> show non-relative opening hours
            return context.getString(R.string.opening_hours) + ": " +
                    DateTimeUtils.getTimeString(opens) + " - " +
                    DateTimeUtils.getTimeString(closes)
        }
    }

    private fun stringToDateTime(date: DateTime, time: String): DateTime {
        return if (time.contains(":")) {
            val hour = Integer.parseInt(time.substring(0, time.indexOf(':')))
            val min = Integer.parseInt(time.substring(time.indexOf(':') + 1))
            date.withHourOfDay(hour)
                    .withMinuteOfHour(min)
        } else {
            date.withHourOfDay(Integer.parseInt(time))
                    .withMinuteOfHour(0)
        }
    }
}