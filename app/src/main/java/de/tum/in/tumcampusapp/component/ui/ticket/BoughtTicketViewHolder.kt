package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketInfo
import de.tum.`in`.tumcampusapp.utils.Utils

class BoughtTicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val amountTextView: TextView by lazy { itemView.findViewById<TextView>(R.id.ticket_amount) }
    private val ticketTypeNameTextView: TextView by lazy { itemView.findViewById<TextView>(R.id.ticket_type_name) }
    private val ticketPriceTextView: TextView by lazy { itemView.findViewById<TextView>(R.id.price_per_ticket) }

    fun bind(ticketInfo: TicketInfo) {
        amountTextView.text = itemView.context.getString(R.string.amount_x, ticketInfo.count)
        ticketTypeNameTextView.text = ticketInfo.ticketType?.description
        ticketPriceTextView.text = itemView.context.getString(R.string.price_per_ticket,
                Utils.formatPrice(ticketInfo.ticketType?.price ?: 0))
    }
}
