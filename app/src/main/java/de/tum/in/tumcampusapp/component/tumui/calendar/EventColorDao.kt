package de.tum.`in`.tumcampusapp.component.tumui.calendar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.EventColor

@Dao
interface EventColorDao {

    @Query("SELECT * FROM event_color_table WHERE event_nr = :eventNr AND event_identifier = :identifier AND is_single_event = :isSingleEvent")
    fun getByEventNrAndIdentifierAndIsSingleEvent(eventNr: String, identifier: String, isSingleEvent: Boolean): List<EventColor>

    @Query("SELECT * FROM event_color_table WHERE event_identifier = :identifier AND is_single_event = :isSingleEvent")
    fun getByIdentifierAndIsSingleEvent(identifier: String, isSingleEvent: Boolean): List<EventColor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(eventColor: EventColor)

    @Query("DELETE FROM event_color_table WHERE event_nr = :eventNr")
    fun deleteByEventNr(eventNr: String)
}
