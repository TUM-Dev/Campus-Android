package de.tum.`in`.tumcampusapp.component.ui.news.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings
import com.google.gson.annotations.SerializedName


/**
 * New News
 *
 * @param id      News source id
 * @param title   Title
 * @param icon    TODO
 */
@Entity(tableName = "news_sources")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class NewsSources(@PrimaryKey
                       @SerializedName("source")
                       var id: Int = -1,
                       var title: String = "",
                       var icon: String = "")