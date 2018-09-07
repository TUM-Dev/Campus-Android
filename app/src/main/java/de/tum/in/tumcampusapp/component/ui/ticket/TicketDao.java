package de.tum.in.tumcampusapp.component.ui.ticket;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;

@Dao
public interface TicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ticket... ticket);

    @Query("SELECT * FROM tickets")
    LiveData<List<Ticket>> getAll();

    @Query("SELECT * FROM tickets where event_id = :eventId")
    Ticket getByEventId(int eventId);

    @Query("DELETE FROM tickets")
    void flush();
}
