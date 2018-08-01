package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
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