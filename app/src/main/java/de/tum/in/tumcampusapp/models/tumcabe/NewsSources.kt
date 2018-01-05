package de.tum.`in`.tumcampusapp.models.tumcabe

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


/**
 * New News
 *
 * @param id      News source id
 * @param title   Title
 * @param icon    TODO
 */
@Entity
data class NewsSources(@PrimaryKey
                       var id: Int = -1,
                       var title: String = "",
                       var icon: String = "")