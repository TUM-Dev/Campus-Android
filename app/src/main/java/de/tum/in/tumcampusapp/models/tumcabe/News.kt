package de.tum.`in`.tumcampusapp.models.tumcabe

import de.tum.`in`.tumcampusapp.auxiliary.Utils
import java.util.*

/**
 * News Object
 */
class News
/**
 * New News

 * @param id News Facebook-ID
 * *
 * @param title Title
 * *
 * @param link Url, e.g. http://www.in.tum.de
 * *
 * @param image Image url e.g. http://www.tu-film.de/img/film/poster/Fack%20ju%20Ghte.jpg
 * *
 * @param date Date
 * *
 * @param created Creation date
 */
(
        /**
         * News Facebook-ID
         */
        val id: String,
        /**
         * Content
         */
        val title: String,
        /**
         * Link Url, e.g. http://www.in.tum.de
         */
        val link: String, val src: String,
        /**
         * Local image, e.g. /mnt/sdcard/tumcampus/news/cache/xy.jpg
         */
        val image: String,
        /**
         * Date
         */
        val date: Date, val created: Date) {

    override fun toString(): String {
        return "id=$id title=$title link=$link image=" + image + " date=" + Utils.getDateString(date) + " created=" + Utils.getDateString(created)
    }
}