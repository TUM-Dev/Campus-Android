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
    void cleanUp();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Event> event);

    @Query("SELECT * FROM event ORDER BY start")
    List<Event> getAll();

    // Get earliest event
    @Query("SELECT * FROM event ORDER BY start LIMIT 1")
    Event getNextEvent();

    @Query("SELECT count(*) FROM event WHERE start < :date")
    int getPosition(String date);

    @Query("SELECT * FROM event ORDER BY start LIMIT 1 OFFSET :position")
    Event getByPosition(int position);

    @Query("SELECT * FROM event where id = :id")
    Event getEventById(int id);

    @Query("DELETE FROM event")
    void flush();

}
