package de.tum.`in`.tumcampusapp.component.ui.ticket.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.ticket.TicketAmountViewHolder
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType

class TicketAmountAdapter(private val ticketTypes: List<TicketType>) : RecyclerView.Adapter<TicketAmountViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketAmountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ticket_amount_row, parent, false)
        return TicketAmountViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketAmountViewHolder, position: Int) {
        holder.bindToTicketType(ticketTypes[position], position)
    }

    override fun getItemCount() = ticketTypes.size
}
