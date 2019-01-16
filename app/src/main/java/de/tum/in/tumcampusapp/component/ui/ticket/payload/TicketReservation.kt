package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import com.google.gson.annotations.SerializedName

data class TicketReservation(@SerializedName("ticket_types")
                             var ticketType: Array<Int> = emptyArray(),
                             var amounts: Array<Int> = emptyArray())
