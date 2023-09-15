package de.tum.`in`.tumcampusapp.component.ui.news.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.google.gson.annotations.SerializedName
import de.tum.`in`.tumcampusapp.api.backend.NewsSourceReply

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
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class NewsSources(
    @PrimaryKey
    @SerializedName("source")
    var id: Int = -1,
    var title: String = "",
    var icon: String = ""
) {

    val isNewspread: Boolean
        get() = setOf(7, 8, 9, 13).contains(id)

    companion object {
        fun fromProto(it: NewsSourceReply): List<NewsSources> {
            return it.sourcesList.map { NewsSources(
                id = it.source.toInt(),
                title = it.title,
                icon = it.icon
            ) }
        }
    }
}
