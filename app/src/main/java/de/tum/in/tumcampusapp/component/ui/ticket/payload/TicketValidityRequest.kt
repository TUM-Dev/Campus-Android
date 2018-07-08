package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import com.google.gson.annotations.SerializedName

data class TicketValidityRequest(@SerializedName("event_id") val eventId: String,
                                 val code: String)
