package de.tum.in.tumcampusapp.component.ui.ticket;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import io.reactivex.Observable;

@Dao
public interface TicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ticket... ticket);

    @Query("SELECT * FROM tickets")
    Observable<List<Ticket>> getAll();

    @Query("SELECT * FROM tickets WHERE event_id = :eventId")
    Ticket getByEventId(int eventId);

    @Query("SELECT count(*) FROM tickets WHERE event_id = :eventId")
    int countEventsWithId(int eventId);

    @Query("DELETE FROM tickets")
    void flush();
}
