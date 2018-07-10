package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Ticket
 *
 * @param event  Event
 * @param code   code
 * @param ticketTypeId   Type
 */
@Entity
data class Ticket(@PrimaryKey
                  @SerializedName("ticket_history")
                  var id: Int,
                  @SerializedName("event")
                  var eventId: Int,
                  var code: String = "",
                  @SerializedName("ticket_type")
                  var ticketTypeId: Int,
                  var redeemed: Boolean = false)
