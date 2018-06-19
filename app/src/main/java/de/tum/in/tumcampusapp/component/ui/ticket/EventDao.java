package de.tum.in.tumcampusapp.component.ui.ticket;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import io.reactivex.Flowable;

@Dao
public interface EventDao {

    /**
     * Removes all old items
     */
    @Query("DELETE FROM event WHERE date < date('now')")
    void cleanUp();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Event event);

    @Query("SELECT * FROM event ORDER BY date")
    Flowable<List<Event>> getAll();

    @Query("SELECT id FROM event ORDER BY id DESC LIMIT 1")
    int getLatestId();

    @Query("SELECT count(*) FROM event WHERE date < :date")
    int getPosition(String date);

    @Query("SELECT * FROM event ORDER BY date LIMIT 1 OFFSET :position")
    Flowable<Event> getByPosition(int position);

    @Query("DELETE FROM event")
    void flush();
}
