package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.google.gson.annotations.SerializedName
import java.text.DecimalFormat

/**
 * Ticket
 *
 * @param id      Ticket-ID
 * @param price   Price
 * @param description   Description
 */
@Entity(tableName = "ticket_types")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class TicketType(
        @PrimaryKey
        @SerializedName("ticket_type")
        var id: Int = 0,
        var price: Int = 0,
        var description: String = ""
) {

    val formattedPrice: String
        get() = DecimalFormat("#.00").format(price / 100.0) + " €"

}