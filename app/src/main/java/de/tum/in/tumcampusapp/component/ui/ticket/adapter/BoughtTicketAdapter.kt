package de.tum.`in`.tumcampusapp.component.ui.ticket.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.ticket.BoughtTicketViewHolder
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketInfo

class BoughtTicketAdapter(private val ticketInfos: List<TicketInfo>) : RecyclerView.Adapter<BoughtTicketViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): BoughtTicketViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.bought_ticket_row, parent, false)
        return BoughtTicketViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: BoughtTicketViewHolder, i: Int) {
        viewHolder.bind(ticketInfos[i])
    }

    override fun getItemCount() = ticketInfos.size
}