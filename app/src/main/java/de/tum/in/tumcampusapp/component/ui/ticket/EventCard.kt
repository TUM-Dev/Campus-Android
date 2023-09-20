package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavDestination
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.ticket.adapter.EventsAdapter
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoActivity
import de.tum.`in`.tumcampusapp.utils.Const

class EventCard(context: Context) : Card(CardManager.CardTypes.EVENT, context) {

    var event: Event? = null

    // TODO(thellmund) Inject this
    private val eventCardsProvider = EventCardsProvider()

    override fun getNavigationDestination(): NavDestination {
        val args = Bundle().apply { putInt(Const.KINO_ID, 1) }
        return NavDestination.Activity(KinoActivity::class.java, args)
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
            val card = LayoutInflater.from(parent.context).inflate(R.layout.card_events_item, parent, false)
            return EventsAdapter.EventViewHolder(card, interactionListener, true)
        }
    }
}
