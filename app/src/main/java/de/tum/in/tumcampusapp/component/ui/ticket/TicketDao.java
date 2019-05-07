package de.tum.in.tumcampusapp.component.ui.ticket;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketInfo;

@Dao
public interface TicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ticket... ticket);

    @Query("SELECT count(*) as count, t.*, tt.* FROM tickets t, ticket_types tt "
           + "WHERE t.event_id = :eventId "
           + "AND t.ticket_type_id = tt.id "
           + "GROUP BY t.ticket_type_id")
    List<TicketInfo> getByEventId(int eventId);

    @Query("SELECT count(*) FROM tickets WHERE event_id =:eventId")
    int getTicketCountForEvent(int eventId);

    @Query("DELETE FROM tickets")
    void flush();
}
