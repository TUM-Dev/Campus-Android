package de.tum.`in`.tumcampusapp.component.ui.ticket.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventCard
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventBetaInfo
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventItem

class EventsAdapter(private val mContext: Context) : RecyclerView.Adapter<CardViewHolder>() {

    private var events: List<EventItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        if (viewType == CARD_INFO) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_events_info, parent, false)
            return CardViewHolder(view)
        }

        val layoutRes = if (viewType == CARD_HORIZONTAL) {
            R.layout.card_events_item
        } else {
            R.layout.card_events_item_vertical
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return EventViewHolder(view, null)
    }

    override fun getItemViewType(position: Int): Int {
        val item = events[position]
        if (item is EventBetaInfo) {
            return CARD_INFO
        }
        return if ((item as Event).kino == -1) {
            CARD_HORIZONTAL
        } else {
            CARD_VERTICAL
        }
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val eventItem = events[position]
        if (eventItem is EventBetaInfo) {
            return
        }
        val event = eventItem as Event
        val eventCard = EventCard(mContext)
        eventCard.event = event
        holder.currentCard = eventCard
    }

    override fun getItemCount() = events.size

    fun update(_es: MutableList<EventItem>) {
    }

    class EventViewHolder(view: View, interactionListener: CardInteractionListener?) : CardViewHolder(view, interactionListener)

    companion object {
        private const val CARD_INFO = 0
        private const val CARD_HORIZONTAL = 1
        private const val CARD_VERTICAL = 2
    }
}
