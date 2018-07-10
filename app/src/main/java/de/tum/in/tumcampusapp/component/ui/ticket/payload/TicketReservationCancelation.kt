package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName


data class TicketReservationCancelation(@SerializedName("ticket_history")
                                        var ticketHistory: Int = 0)