package de.tum.`in`.tumcampusapp.component.ui.news.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName


/**
 * This class contains information about the source of a [News] item.
 *
 * Find the currently available news sources at [https://app.tum.de/api/news/sources].
 *
 * @param id The ID of the news source
 * @param title The title of the news source
 * @param icon The image URL of the icon of the news source
 */
@Entity(tableName = "news_sources")
data class NewsSources(@PrimaryKey
                       @SerializedName("source")
                       var id: Int = -1,
                       var title: String = "",
                       var icon: String = "") {

    val isNewspread: Boolean
        get() = setOf(7, 8, 9, 13).contains(id)

}