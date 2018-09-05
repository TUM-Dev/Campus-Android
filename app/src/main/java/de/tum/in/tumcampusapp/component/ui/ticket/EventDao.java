package de.tum.in.tumcampusapp.component.ui.ticket;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;

@Dao
public interface EventDao {

    @Query("SELECT * FROM events ORDER BY start_time")
    LiveData<List<Event>> getAll();

    @Query("SELECT * FROM events WHERE start_time > date('now') ORDER BY start_time LIMIT 1")
    Event getNextEvent();

    @Query("SELECT * FROM events where id = :id")
    Event getEventById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Event> events);

    @Query("UPDATE events SET dismissed = 1 WHERE id = :eventId")
    void setDismissed(int eventId);

    @Query("DELETE FROM events WHERE start_time < date('now')")
    void removePastEvents();

    @Query("DELETE FROM events")
    void removeAll();

}
