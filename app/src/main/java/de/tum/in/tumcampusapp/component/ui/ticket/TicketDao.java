package de.tum.in.tumcampusapp.component.ui.ticket;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import io.reactivex.Flowable;

@Dao
public interface TicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ticket ticket);

    @Query("SELECT * FROM ticket")
    Flowable<List<Ticket>> getAll();

    @Query("DELETE FROM ticket")
    void flush();

}
