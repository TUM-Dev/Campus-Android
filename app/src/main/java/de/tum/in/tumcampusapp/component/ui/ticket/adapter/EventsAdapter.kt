package de.tum.`in`.tumcampusapp.component.ui.ticket.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventItem

class EventsAdapter : RecyclerView.Adapter<CardViewHolder>() {

    private var events: List<EventItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val layoutRes = R.layout.card_events_item_vertical
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return EventViewHolder(view, null)
    }

    override fun getItemViewType(position: Int): Int {
        return CARD_INFO
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
    }

    override fun getItemCount() = events.size

    fun update(_es: MutableList<EventItem>) {
    }

    class EventViewHolder(view: View, interactionListener: CardInteractionListener?) : CardViewHolder(view, interactionListener)

    companion object {
        private const val CARD_INFO = 0
    }
}
