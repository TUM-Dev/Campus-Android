package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventSeriesMappings")
data class EventSeriesMapping(
        var seriesId: String = "",
        var eventId: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
