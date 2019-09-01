package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import androidx.room.Entity
import androidx.room.RoomWarnings

/**
 * Entity for storing blacklisted lectures to not be shown in the widget
 */
@Entity(tableName = "widgets_timetable_blacklist", primaryKeys = ["widget_id", "lecture_title"])
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class WidgetsTimetableBlacklist(
    var widget_id: Int = -1,
    var lecture_title: String = ""
)