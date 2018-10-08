package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings
import android.content.Context
import android.text.format.DateFormat
import com.google.gson.annotations.SerializedName
import de.tum.`in`.tumcampusapp.R
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

    fun getFormattedRedemptionDate(context: Context): String? {
        return if (redemption != null) {
            val date = DateTimeFormat.shortDate().print(redemption)
            val pattern = if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm aa"
            val time = DateTimeFormat.forPattern(pattern).print(redemption)
            "$date, $time"
        } else {
            context.getString(R.string.no)
        }
    }


}
