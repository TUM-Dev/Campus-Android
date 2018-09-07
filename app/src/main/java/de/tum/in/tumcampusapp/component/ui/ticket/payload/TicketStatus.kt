package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import com.google.gson.annotations.SerializedName

data class TicketStatus(@SerializedName("ticket_type")
                        var ticketType: Int = 0,
                        var contingent: Int = 0,
                        var sold: Int = 0) {
    val availableTicketCount: Int
        get() = contingent - sold
}