package de.tum.`in`.tumcampusapp.component.ui.ticket

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType

@Dao
interface TicketTypeDao {

    @get:Query("SELECT * FROM ticket_types")
    val all: List<TicketType>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ticketTypes: List<TicketType>)

    @Query("DELETE FROM ticket_types")
    fun flush()
}
