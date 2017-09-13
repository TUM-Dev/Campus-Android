package de.tum.`in`.tumcampusapp.models.tumcabe

import de.tum.`in`.tumcampusapp.auxiliary.Utils
import java.util.*

/**
 * Kino Object
 */
class Kino
/**
 * Kino Constructor
 * @param id ID
 * *
 * @param title Title
 * *
 * @param year Year
 * *
 * @param runtime Runtime
 * *
 * @param genre Genre
 * *
 * @param director Director
 * *
 * @param actors Actors
 * *
 * @param rating IMDB-Rating
 * *
 * @param description Description
 * *
 * @param cover Cover
 * *
 * @param trailer Trailer
 * *
 * @param date Date
 * *
 * @param created Created
 * *
 * @param link Link
 */
(// all entries in the kino database
        val id: String, val title: String, val year: String, val runtime: String, val genre: String, val director: String, val actors: String,
        val rating: String, val description: String, val cover: String, val trailer: String, val date: Date, val created: Date, val link: String) {

    override fun toString(): String {
        return "id=" + id + " title=" + title + " year=" + year + " runtime=" + runtime +
                " genre=" + genre + " director=" + director + " actors=" + actors + " rating=" + rating +
                " description=" + description + " cover=" + cover + " trailer=" + trailer +
                " date=" + Utils.getDateString(date) + " created=" + Utils.getDateString(created) + " link=" + link
    }

}

