package de.tum.`in`.tumcampusapp.component.ui.ticket

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketInfo

@Dao
interface TicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg ticket: Ticket)

    @Query("SELECT count(*) as count, t.*, tt.* FROM tickets t, ticket_types tt " +
            "WHERE t.event_id = :eventId " +
            "AND t.ticket_type_id = tt.id " +
            "GROUP BY t.ticket_type_id")
    fun getByEventId(eventId: Int): List<TicketInfo>

    @Query("SELECT count(*) FROM tickets WHERE event_id =:eventId")
    fun getTicketCountForEvent(eventId: Int): Int

    @Query("DELETE FROM tickets")
    fun flush()
}
