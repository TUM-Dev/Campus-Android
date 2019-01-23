package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import androidx.room.Embedded
import androidx.room.Relation

class TicketInfo {
    @Embedded
    var ticketType: TicketType? = null
    @Relation(entityColumn =  "ticket_type_id", parentColumn = "id")
    var ticket: List<Ticket>? = null
    var count: Int = 0
}
