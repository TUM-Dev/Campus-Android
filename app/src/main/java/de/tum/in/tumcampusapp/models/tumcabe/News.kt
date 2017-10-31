package de.tum.`in`.tumcampusapp.models.tumcabe

import java.util.*

/**
 * New News
 *
 * @param id      News Facebook-ID
 * @param title   Title
 * @param link    Url, e.g. http://www.in.tum.de
 * @param image   Image url e.g. http://www.tu-film.de/img/film/poster/Fack%20ju%20Ghte.jpg
 * @param date    Date
 * @param created Creation date
 */
data class News(val id: String, val title: String, val link: String, val src: String, val image: String, val date: Date, val created: Date)