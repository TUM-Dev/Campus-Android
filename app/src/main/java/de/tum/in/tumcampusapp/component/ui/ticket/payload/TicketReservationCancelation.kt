package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity
data class TicketReservationCancelation(@PrimaryKey
                        @SerializedName("ticket_history")
                      var ticket_history: Int = 0)