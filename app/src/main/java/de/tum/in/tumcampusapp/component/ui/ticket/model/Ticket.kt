package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Ticket
 *
 * @param id  ID of ticket_history in DB
 * @param event   Event ID
 * @param code   Ticket Code
 * @param ticketTypeId  ID of TicketType
 * @param redeemed
 */
@Entity
data class Ticket(@PrimaryKey
                  @SerializedName("ticket_history")
                  var id: Int = 0,
                  @SerializedName("event")
                  var eventId: Int = 0,
                  var code: String = "",
                  @SerializedName("ticket_type")
                  var ticketTypeId: Int = 0,
                  var redeemed: Boolean = false)
