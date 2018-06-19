package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.DateUtils
import de.tum.`in`.tumcampusapp.utils.sync.model.Sync
import io.reactivex.Flowable
import java.util.*

object EventLocalRepository {

    private const val TIME_TO_SYNC = 1800

    lateinit var db: TcaDb

    fun getLastSync() = db.syncDao().getSyncSince(Event::class.java.name, TIME_TO_SYNC)

    fun updateLastSync() = db.syncDao().insert(Sync(Event::class.java.name, DateUtils.getDateTimeString(Date())))

    fun addEvent(event: Event) = db.eventDao().insert(event)

    fun getAllEvents(): Flowable<List<Event>> = db.eventDao().all

    fun getLatestId(): Int? = db.eventDao().latestId

    fun getEventByPosition(position: Int): Flowable<Event> = db.eventDao().getByPosition(position)

    fun clear() = db.eventDao().cleanUp()

    fun getPosition(date: String) = db.eventDao().getPosition(date)

    // Ticket methods
    fun getAllTickets() : Flowable<List<Ticket>> = db.ticketDao().all
    // TODO: implement methods for all ticket calls here

}