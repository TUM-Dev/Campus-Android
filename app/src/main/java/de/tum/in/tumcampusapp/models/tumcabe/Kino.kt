package de.tum.`in`.tumcampusapp.models.tumcabe

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
data class Kino(val id: String, val title: String, val year: String, val runtime: String, val genre: String, val director: String, val actors: String,
                val rating: String, val description: String, val cover: String, val trailer: String, val date: Date, val created: Date, val link: String)

