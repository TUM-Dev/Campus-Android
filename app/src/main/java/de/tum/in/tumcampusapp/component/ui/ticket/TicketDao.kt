package de.tum.`in`.tumcampusapp.component.ui.ticket

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket

@Dao
interface TicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg ticket: Ticket)

    @Query("DELETE FROM tickets")
    fun flush()
}
