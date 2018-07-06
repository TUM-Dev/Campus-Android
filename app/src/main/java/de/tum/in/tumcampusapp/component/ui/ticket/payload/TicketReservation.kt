package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity
data class TicketReservation(@PrimaryKey
                             @SerializedName("ticket_type")
                             var id: Int = 0)
