package de.tum.`in`.tumcampusapp.component.ui.tufilm.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

/**
 * Kino Constructor
 *
 * @param id          ID
 * @param title       Title
 * @param year        Year
 * @param runtime     Runtime
 * @param genre       Genre
 * @param director    Director
 * @param actors      Actors
 * @param rating      IMDB-Rating
 * @param description Description
 * @param cover       Cover
 * @param trailer     Trailer
 * @param date        Date
 * @param created     Created
 * @param link        Link
 */
@Entity
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class Kino(@PrimaryKey
                @SerializedName("kino")
                var id: String = "",
                var title: String = "",
                var year: String = "",
                var runtime: String = "",
                var genre: String = "",
                var director: String = "",
                var actors: String = "",
                var rating: String = "",
                var description: String = "",
                var cover: String = "",
                var trailer: String? = "",
                var date: DateTime = DateTime(),
                var created: DateTime = DateTime(),
                var link: String = "") {

    val formattedDate: String
        get() {
            val formatter = DateTimeFormat.mediumDate().withLocale(Locale.getDefault())
            return formatter.print(date)
        }

    val formattedDescription: String
        get() {
            return description
                    .replace("\n", "")
                    .replace("\r", "\r\n")
                    .removeSuffix("\r\n")
        }

    val trailerSearchUrl: String
        get() {
            val actualTitle = title.split(": ")[1]
            return "https://www.youtube.com/results?search_query=Trailer $actualTitle".replace(" ", "+")
        }

    fun isFutureMovie() = date.isAfterNow

}

