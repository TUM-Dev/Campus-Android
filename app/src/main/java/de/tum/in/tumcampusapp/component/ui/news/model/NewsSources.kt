package de.tum.`in`.tumcampusapp.component.ui.news.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


/**
 * New News
 *
 * @param id      News source id
 * @param title   Title
 * @param icon    TODO
 */
@Entity(tableName = "news_sources")
data class NewsSources(@PrimaryKey
                       var id: Int = -1,
                       var title: String = "",
                       var icon: String = "")