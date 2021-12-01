package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "eventSeriesMappings")
data class EventSeriesMapping(
    var seriesId: String = "",
    var eventId: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @Ignore
    // Needed to tell Room to use the constructor with two arguments as kotlin auto-generates the no-arg constructor
    // and room picks it when multiple constructors are present
    constructor() : this("", "")
}
