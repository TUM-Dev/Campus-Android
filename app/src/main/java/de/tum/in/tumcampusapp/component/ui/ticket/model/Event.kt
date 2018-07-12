package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Event
 *
 * @param id      Event-ID
 * @param image   Image url e.g. http://www.tu-film.de/img/film/poster/Fack%20ju%20Ghte.jpg
 * @param title   Title
 * @param description Description
 * @param locality Locality
 * @param start   Date
 * @param end     Date
 * @param link    Url, e.g. http://www.in.tum.de
 */
@Entity
data class Event(@PrimaryKey
                 @SerializedName("event")
                 var id: Int = 0,
                 @SerializedName("file")
                 var image: String? = null,
                 var title: String = "",
                 var description: String = "",
                 var locality: String = "",
                 var start: DateTime = DateTime(),
                 var end: DateTime? = null,
                 var link: String = "") {

    companion object {
        const val defaultDuration = 7200000 // Milliseconds

        fun getFormattedDateTime(context: Context, dateTime: DateTime): String {
            return "${getFormattedDate(dateTime)} ${getFormattedTime(context, dateTime)}"
        }

        fun getFormattedDate(dateTime: DateTime): String {
            return DateTimeFormat.shortDate().print(dateTime)
        }

        fun getFormattedTime(context: Context, dateTime: DateTime): String {
            var pattern = if (android.text.format.DateFormat.is24HourFormat(context)) "H:mm" else "h:mm aa"
            return DateTimeFormat.forPattern(pattern).print(dateTime)
        }
    }

    fun isFutureEvent() = start.isAfter(DateTime())

}