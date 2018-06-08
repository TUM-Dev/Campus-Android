package de.tum.`in`.tumcampusapp.component.ui.news.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.gson.annotations.SerializedName
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoActivity
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateUtils
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
                @SerializedName("news")
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

    fun getIntent(context: Context): Intent? {
        return if (isFilm()) {
            Intent(context, KinoActivity::class.java).apply {
                putExtra(Const.KINO_DATE, DateUtils.getDateTimeString(date))
            }
        } else {
            if (link.isBlank()) null else Intent(Intent.ACTION_VIEW, Uri.parse(link))
        }
    }

}