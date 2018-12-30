package de.tum.in.tumcampusapp.component.ui.ticket;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;

@Dao
public interface TicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ticket... ticket);

    @Query("SELECT * FROM tickets")
    LiveData<List<Ticket>> getAll();

    @Query("SELECT * FROM tickets WHERE event_id = :eventId")
    List<Ticket> getByEventId(int eventId);

    @Query("SELECT count(*) FROM tickets WHERE event_id =:eventId")
    int getTicketCountForEvent(int eventId);

    @Query("DELETE FROM tickets")
    void flush();
}
