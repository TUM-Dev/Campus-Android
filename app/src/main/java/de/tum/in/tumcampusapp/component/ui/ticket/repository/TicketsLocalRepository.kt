package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType
import de.tum.`in`.tumcampusapp.database.TcaDb
import io.reactivex.Observable
import javax.inject.Inject

class TicketsLocalRepository @Inject constructor(
        private val database: TcaDb
) {

    fun getAll(): Observable<List<Ticket>> = database.ticketDao().all

    fun storeTickets(tickets: List<Ticket>) {
        insert(*tickets.toTypedArray())
    }

    fun insert(vararg tickets: Ticket) {
        database.ticketDao().insert(*tickets)
    }

    fun getTicketByEventId(eventId: Int): Ticket = database.ticketDao().getByEventId(eventId)

    fun isEventBooked(event: Event): Boolean = database.ticketDao().countEventsWithId(event.id) > 0

    fun getTicketTypeById(id: Int): TicketType = database.ticketTypeDao().getById(id)

    fun addTicketTypes(ticketTypes: List<TicketType>) {
        database.ticketTypeDao().insert(ticketTypes)
    }

}
