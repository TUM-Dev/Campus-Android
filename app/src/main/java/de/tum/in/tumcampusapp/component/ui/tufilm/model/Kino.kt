package de.tum.`in`.tumcampusapp.component.ui.tufilm.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

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

    fun isFutureMovie() = date.isAfterNow
}

