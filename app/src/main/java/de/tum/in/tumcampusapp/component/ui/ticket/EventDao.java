package de.tum.in.tumcampusapp.component.ui.ticket;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;

@Dao
public interface EventDao {

    /**
     * Removes all old items
     */
    @Query("DELETE FROM event WHERE start < date('now')")
    void removePastEvents();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Event> events);

    @Query("SELECT * FROM event ORDER BY start")
    List<Event> getAll();

    @Query("SELECT * FROM event where id = :id")
    Event getEventById(int id);

    @Query("DELETE FROM event")
    void removeAll();

}
