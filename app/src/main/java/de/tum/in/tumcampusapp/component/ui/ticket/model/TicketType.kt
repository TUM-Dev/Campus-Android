package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Ticket
 *
 * @param id      Ticket-ID
 * @param price   Price
 * @param price   Description
 */
@Entity
data class TicketType(@PrimaryKey
                  @SerializedName("tickettype")
                  var id: Int = 0,
                  var price: Double = 0.toDouble(),
                  var description: String = "")