package de.tum.in.tumcampusapp.component.ui.ticket;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import io.reactivex.Flowable;
import io.reactivex.Observable;

@Dao
public interface EventDao {

    @Query("SELECT * FROM events WHERE start_time > date('now') ORDER BY start_time")
    Observable<List<Event>> getAllFutureEvents();

    @Query("SELECT * " +
            "FROM events " +
            "WHERE EXISTS (SELECT * FROM tickets WHERE tickets.event_id = events.id) " +
            "ORDER BY start_time")
    Observable<List<Event>> getAllBookedEvents();

    @Query("SELECT * " +
            "FROM events " +
            "WHERE start_time > date('now') " +
            "AND events.kino = -1 " +
            "ORDER BY start_time " +
            "LIMIT 1")
    Event getNextEventWithoutMovie();

    @Query("SELECT * FROM events where id = :id")
    Event getEventById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Event> events);

    @Query("UPDATE events SET dismissed = 1 WHERE id = :eventId")
    void setDismissed(int eventId);

    @Query("DELETE FROM events " +
            "WHERE end_time < date('now') " +
            "AND NOT EXISTS (SELECT * FROM tickets WHERE tickets.event_id = events.id)")
    void removePastEventsWithoutTicket();

    @Query("DELETE FROM events")
    void removeAll();

    @Query("SELECT events.* FROM events, kino " +
            "WHERE events.kino =:kinoId " +
            "LIMIT 1")
    Flowable<Event> getEventByMovie(String kinoId);

    @Query("SELECT count(*) FROM events, kino " +
            "WHERE kino.link =:eventLink " +
            "LIMIT 1")
    int getKinoCountForEvent(String eventLink);

}
