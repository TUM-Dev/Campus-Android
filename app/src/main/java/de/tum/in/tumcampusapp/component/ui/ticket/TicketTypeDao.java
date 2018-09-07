package de.tum.in.tumcampusapp.component.ui.ticket;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;

@Dao
public interface TicketTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<TicketType> ticketTypes);

    @Query("SELECT * FROM ticket_types")
    List<TicketType> getAll();

    @Query("SELECT * FROM ticket_types WHERE id = :id")
    TicketType getById(int id);

    @Query("DELETE FROM ticket_types")
    void flush();
}
