package de.tum.`in`.tumcampusapp.models.tumo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import de.tum.`in`.tumcampusapp.auxiliary.Utils
import de.tum.`in`.tumcampusapp.auxiliary.calendar.IntegratedCalendarEvent
import java.util.*
import java.util.regex.Pattern

@Entity(tableName="calendar")
data class CalendarItem(@PrimaryKey
                        var nr: String = "",
                        var status: String = "",
                        var url: String = "",
                        var title: String = "",
                        var description: String = "",
                        var dtstart: String = "",
                        var dtend: String = "",
                        var location: String = "",
                        @Ignore
                        var blacklisted: Boolean = false) {
    /**
     * Returns the color of the event
     */
    fun getEventColor(): Int {
        return if (title.endsWith("VO") || title.endsWith("VU")) {
            IntegratedCalendarEvent.getDisplayColorFromColor(-0xd76de1)
        } else if (title.endsWith("UE")) {
            IntegratedCalendarEvent.getDisplayColorFromColor(-0x8000)
        } else {
            IntegratedCalendarEvent.getDisplayColorFromColor(-0xffff01)
        }
    }

    /**
     * Get event start as Calendar object
     */
    fun getEventStart(): Calendar {
        val result = Calendar.getInstance()
        result.time = Utils.getDateTime(dtstart)
        return result
    }

    /**
     * Get event end as Calendar object
     */
    fun getEventEnd(): Calendar {
        val result = Calendar.getInstance()
        result.time = Utils.getDateTime(dtend)
        return result
    }

    fun getFormattedTitle(): String {
        return Pattern.compile("\\([A-Z0-9\\.]+\\)")
                .matcher(Pattern.compile("\\([A-Z]+[0-9]+\\)")
                        .matcher(Pattern.compile("[A-Z, 0-9(LV\\.Nr)=]+$")
                                .matcher(title)
                                .replaceAll(""))
                        .replaceAll(""))
                .replaceAll("")!!
                .trim { it <= ' ' }
    }

    fun getEventLocation(): String {
        return Pattern.compile("\\([A-Z0-9\\.]+\\)")
                .matcher(location)
                .replaceAll("")!!
                .trim { it <= ' ' }
    }
}