package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.content.Context
import android.text.format.DateFormat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Ticket
 *
 * @param id  ID of ticket_history in DB
 * @param event   Event ID
 * @param code   Ticket Code
 * @param ticketTypeId  ID of TicketType
 * @param redeemed
 */
@Entity(tableName = "tickets")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class Ticket(
        @PrimaryKey
        @SerializedName("ticket_history")
        var id: Int = 0,
        @ColumnInfo(name = "event_id")
        @SerializedName("event")
        var eventId: Int = 0,
        var code: String = "",
        @ColumnInfo(name = "ticket_type_id")
        @SerializedName("ticket_type")
        var ticketTypeId: Int = 0,
        var redemption: DateTime? = null
) {

    companion object {
        fun getFormattedRedemptionDate(context: Context, redemptionDate: DateTime): String? {
            val date = DateTimeFormat.shortDate().print(redemptionDate)
            val pattern = if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm aa"
            val time = DateTimeFormat.forPattern(pattern).print(redemptionDate)
            return "$date, $time"
        }
    }


}
