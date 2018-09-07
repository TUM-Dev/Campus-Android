package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import com.google.gson.annotations.SerializedName

data class TicketReservationResponse(@SerializedName("ticket_history")
                                     var ticketHistory: Int = 0,
                                     var error: String? = null)