package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import com.google.gson.annotations.SerializedName

data class TicketPurchaseStripe(@SerializedName("ticket_ids")
                                var ticketIds: List<Int> = ArrayList(),
                                var token: String = "",
                                @SerializedName("customer_name")
                                var customerName: String = "")
