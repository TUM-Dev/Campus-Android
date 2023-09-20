package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.database.TcaDb

class TicketsLocalRepository constructor(
    private val database: TcaDb
) {

    fun insert(vararg tickets: Ticket) {
        database.ticketDao().insert(*tickets)
    }
}
