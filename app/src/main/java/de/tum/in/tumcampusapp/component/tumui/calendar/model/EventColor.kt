package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_color_table")
data class EventColor(
    @PrimaryKey(autoGenerate = true)
    val eventColorId: Int?,
    @ColumnInfo(name = "event_identifier")
    val eventIdentifier: String,
    @ColumnInfo(name = "event_nr")
    val eventNr: String,
    @ColumnInfo(name = "is_single_event")
    val isSingleEvent: Boolean,
    @ColumnInfo(name = "color")
    val color: Int
)
