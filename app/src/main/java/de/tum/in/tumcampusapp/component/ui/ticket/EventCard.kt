package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavDestination
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.EventDetailsActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.adapter.EventsAdapter
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoActivity
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const

class EventCard(context: Context) : Card(CardManager.CARD_EVENT, context, "card_event") {

    var event: Event? = null

    // TODO(thellmund) Inject this
    private val eventCardsProvider = EventCardsProvider(context, EventsLocalRepository(TcaDb.getInstance(context)))
    private val localRepo = TicketsLocalRepository(TcaDb.getInstance(context))

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        super.updateViewHolder(viewHolder)

        val eventViewHolder = viewHolder as? EventsAdapter.EventViewHolder ?: return

        val event = this.event
        if (event != null) {
            val ticketCount = event.let { localRepo.getTicketCount(it) } ?: 0
            eventViewHolder.bind(event, ticketCount)
        }
    }

    override fun getNavigationDestination(): NavDestination? {
        val event = this.event
        if (event != null && event.kino != -1) {
            val args = Bundle().apply { putInt(Const.KINO_ID, event.kino) }
            return NavDestination.Activity(KinoActivity::class.java, args)
        }
        val args = Bundle().apply { putParcelable(Const.KEY_EVENT, event) }
        return NavDestination.Activity(EventDetailsActivity::class.java, args)
    }

    override fun shouldShow(prefs: SharedPreferences): Boolean {
        return event?.dismissed == 0
    }

    override fun discard(editor: SharedPreferences.Editor) {
        event?.let {
            eventCardsProvider.setDismissed(it.id)
        }
    }

    companion object {

        @JvmStatic
        fun inflateViewHolder(
                parent: ViewGroup,
                interactionListener: CardInteractionListener
        ): CardViewHolder {
            val card = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_events_item, parent, false)
            return EventsAdapter.EventViewHolder(card, interactionListener, true)
        }

    }

}
