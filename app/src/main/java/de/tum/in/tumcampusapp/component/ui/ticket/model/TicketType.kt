package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.google.gson.annotations.SerializedName

/**
 * Ticket
 *
 * @param id Ticket-ID
 * @param price Price
 * @param description Description
 */
@Entity(tableName = "ticket_types")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class TicketType(
    @PrimaryKey
    @SerializedName("ticket_type")
    var id: Int = 0,
    var price: Int = 0,
    var description: String = "",
    @Ignore
    @SerializedName("payment")
    var paymentInfo: Payment = Payment(),
    @Ignore
    var contingent: Int = 0,
    @Ignore
    var sold: Int = 0
)