package de.tum.`in`.tumcampusapp.models.tumcabe

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
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
data class Kino(@PrimaryKey
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
                var trailer: String = "",
                var date: Date = Date(),
                var created: Date = Date(),
                var link: String = "")

