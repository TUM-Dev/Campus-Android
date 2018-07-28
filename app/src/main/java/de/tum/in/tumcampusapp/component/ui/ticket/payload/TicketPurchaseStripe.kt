package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import com.google.gson.annotations.SerializedName

data class TicketPurchaseStripe(@SerializedName("ticket_history")
                                var ticketHistory: Int = 0,
                                var token: String = "",
                                @SerializedName("customer_name")
                                var customerName: String = "")
