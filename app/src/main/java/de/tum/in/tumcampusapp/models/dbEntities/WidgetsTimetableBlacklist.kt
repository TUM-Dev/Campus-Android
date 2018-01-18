package de.tum.`in`.tumcampusapp.models.dbEntities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Entity for storing blacklisted lectures to not be shown in the widget
 */
@Entity(tableName = "widgets_timetable_blacklist",
        primaryKeys = arrayOf("widget_id", "lecture_title"))
data class WidgetsTimetableBlacklist(var widget_id: Int = -1,
                                     var lecture_title: String = "")