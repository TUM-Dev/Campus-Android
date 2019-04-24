package de.tum.in.tumcampusapp.component.ui.ticket;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;

@Dao
public interface TicketTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<TicketType> ticketTypes);

    @Query("SELECT * FROM ticket_types")
    List<TicketType> getAll();

    @Query("SELECT * FROM ticket_types tt, tickets t WHERE tt.id = t.ticket_type_id AND t.event_id = :eventId")
    List<TicketType> getByEventId(int eventId);

    @Query("DELETE FROM ticket_types")
    void flush();
}
