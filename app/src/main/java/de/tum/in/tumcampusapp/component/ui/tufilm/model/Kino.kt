package de.tum.`in`.tumcampusapp.component.ui.tufilm.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.math.roundToInt

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
data class Kino(
        @PrimaryKey
        @SerializedName("kino")
        val id: String,
        val title: String,
        val year: String,
        val runtime: String,
        val genre: String,
        val director: String,
        val actors: String,
        val rating: String,
        val description: String,
        val cover: String,
        val trailer: String?,
        val date: DateTime,
        val created: DateTime,
        val link: String
) {

    val formattedShortDate: String
        get() {
            // e.g. 11/20/2018 8:00 PM (Style SS = short date and short time)
            val shortDate = DateTimeFormat.forPattern("dd MMM").print(date)
            val shortTime = DateTimeFormat.shortTime().withLocale(Locale.getDefault()).print(date)
            return "$shortDate\n$shortTime"
        }

    val formattedDescription: String
        get() {
            return description
                    .replace(".", ". ")   // Add space after full stops
                    .replace("\\s+", " ") // If this results in multiple spaces, reduce them to one
                    .replace("\n", "")
                    .replace("\r", "\r\n")
                    .removeSuffix("\r\n")
        }

    val trailerSearchUrl: String
        get() {
            val actualTitle = title.split(": ")[1]
            return "https://www.youtube.com/results?search_query=Trailer $actualTitle".replace(" ", "+")
        }

    val formattedRating: String
        get() {
            val roundedRating = rating.toDouble().roundToInt()
            return "$roundedRating / 10"
        }

}
