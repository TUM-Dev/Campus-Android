package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import com.google.gson.annotations.SerializedName

data class TicketReservation(@SerializedName("ticket_type")
                             var ticketType: Int = 0,
                             var amount: Int = 1)
