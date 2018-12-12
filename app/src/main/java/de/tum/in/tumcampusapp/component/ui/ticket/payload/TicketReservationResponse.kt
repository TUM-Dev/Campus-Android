package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import com.google.gson.annotations.SerializedName

data class TicketReservationResponse(@SerializedName("ticket_ids")
                                     var ticketIds: ArrayList<Int> = ArrayList(),
                                     var error: String? = null)