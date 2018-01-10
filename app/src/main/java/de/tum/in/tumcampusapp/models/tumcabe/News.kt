package de.tum.`in`.tumcampusapp.models.tumcabe

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
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
@Entity
data class News(@PrimaryKey
                var id: String = "",
                var title: String = "",
                var link: String = "",
                var src: String = "",
                var image: String = "",
                var date: Date = Date(),
                var created: Date = Date(),
                var dismissed: Int = 0) {
    /**
     * Identifies News as a film.
     *
     * @return true if News is a film; else false
     */
    fun isFilm(): Boolean {
        return src == "2"
    }

}