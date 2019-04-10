package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketInfo
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType
import de.tum.`in`.tumcampusapp.database.TcaDb
import javax.inject.Inject

class TicketsLocalRepository @Inject constructor(
        private val database: TcaDb
) {
    fun storeTickets(tickets: List<Ticket>) {
        insert(*tickets.toTypedArray())
    }

    fun insert(vararg tickets: Ticket) {
        database.ticketDao().insert(*tickets)
    }

    fun getTicketsByEventId(eventId: Int): List<TicketInfo> = database.ticketDao().getByEventId(eventId)

    fun getTicketTypeById(id: Int): TicketType = database.ticketTypeDao().getById(id)

    fun addTicketTypes(ticketTypes: List<TicketType>) {
        database.ticketTypeDao().insert(ticketTypes)
    }

    fun getTicketCount(event: Event): Int {
        return database.ticketDao().getTicketCountForEvent(event.id)
    }

}
