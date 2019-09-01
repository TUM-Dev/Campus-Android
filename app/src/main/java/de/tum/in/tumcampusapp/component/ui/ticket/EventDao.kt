package de.tum.`in`.tumcampusapp.component.ui.ticket

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import io.reactivex.Flowable
import io.reactivex.Observable

@Dao
interface EventDao {

    @get:Query("SELECT * FROM events WHERE start_time > date('now') ORDER BY start_time")
    val allFutureEvents: Observable<List<Event>>

    @get:Query("SELECT * " +
            "FROM events " +
            "WHERE EXISTS (SELECT * FROM tickets WHERE tickets.event_id = events.id) " +
            "ORDER BY start_time")
    val allBookedEvents: Observable<List<Event>>

    @get:Query("SELECT * " +
            "FROM events " +
            "WHERE start_time > date('now') " +
            "AND events.kino = -1 " +
            "ORDER BY start_time " +
            "LIMIT 1")
    val nextEventWithoutMovie: Event

    @Query("SELECT * FROM events where id = :id")
    fun getEventById(id: Int): Event?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(events: List<Event>)

    @Query("UPDATE events SET dismissed = 1 WHERE id = :eventId")
    fun setDismissed(eventId: Int)

    @Query("DELETE FROM events " +
            "WHERE end_time < date('now') " +
            "AND NOT EXISTS (SELECT * FROM tickets WHERE tickets.event_id = events.id)")
    fun removePastEventsWithoutTicket()

    @Query("DELETE FROM events")
    fun removeAll()

    @Query("SELECT events.* FROM events, kino " +
            "WHERE events.kino =:kinoId " +
            "LIMIT 1")
    fun getEventByMovie(kinoId: String): Flowable<Event>

    @Query("SELECT count(*) FROM events, kino " +
            "WHERE kino.link =:eventLink " +
            "LIMIT 1")
    fun getKinoCountForEvent(eventLink: String): Int
}
