package de.tum.`in`.tumcampusapp.component.tumui.calendar

import androidx.room.*
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.EventColor

@Dao
interface EventColorDao {

    @Query("SELECT * FROM event_color_table WHERE event_identifier = :identifier")
    fun getByEventIdentifier(identifier: String): List<EventColor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(eventColor: EventColor)
}