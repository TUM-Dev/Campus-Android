package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class TicketReservationResponse(@PrimaryKey
                  @SerializedName("ticket_history")
                  var ticketHistory: Int)