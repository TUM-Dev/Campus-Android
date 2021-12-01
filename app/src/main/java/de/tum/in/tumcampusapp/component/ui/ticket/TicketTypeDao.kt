package de.tum.`in`.tumcampusapp.component.ui.ticket

import androidx.room.*
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType

@Dao
interface TicketTypeDao {

    @get:Query("SELECT * FROM ticket_types")
    val all: List<TicketType>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ticketTypes: List<TicketType>)

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM ticket_types tt, tickets t WHERE tt.id = t.ticket_type_id AND t.event_id = :eventId")
    fun getByEventId(eventId: Int): List<TicketType>

    @Query("DELETE FROM ticket_types")
    fun flush()
}
