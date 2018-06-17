package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Ticket
 *
 * @param event  Event
 * @param code   code
 * @param type   Type
 */
@Entity
data class Ticket(@PrimaryKey
                 @SerializedName("ticket")
                 var event: Event,
                 var code: String = "",
                  @SerializedName("ticket_type")
                 var type: TicketType,
                  @SerializedName("ticket_payment")
                  var payment: Int = 0)