package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavigationDestination
import de.tum.`in`.tumcampusapp.component.other.navigation.SystemActivity
import de.tum.`in`.tumcampusapp.component.other.navigation.SystemIntent
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.EventDetailsActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.adapter.EventsAdapter
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoActivity
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime

class EventCard(context: Context) : Card(CardManager.CARD_EVENT, context, "card_event") {

    var event: Event? = null
    private val eventsController = EventsController(context)

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        super.updateViewHolder(viewHolder)

        val eventViewHolder = viewHolder as? EventsAdapter.EventViewHolder ?: return
        val hasTicket = eventsController.isEventBooked(event)
        eventViewHolder.bind(event, hasTicket)
    }

    override fun getNavigationDestination(): NavigationDestination? {
        if (event != null && eventsController.isKinoEvent(event)) {
            val intent = Intent(context, KinoActivity::class.java)
            intent.putExtra(Const.KINO_DATE,
                    DateTimeUtils.getDateTimeString(event?.startTime ?: DateTime()))
            return SystemIntent(intent)
        }
        val bundle = Bundle().apply { putParcelable("event", event) }
        return SystemActivity(EventDetailsActivity::class.java, bundle)
    }

    override fun shouldShow(prefs: SharedPreferences): Boolean {
        return event?.dismissed == 0
    }

    override fun discard(editor: SharedPreferences.Editor) {
        event?.let {
            eventsController.setDismissed(it.id)
        }
    }

    companion object {

        @JvmStatic
        fun inflateViewHolder(parent: ViewGroup): CardViewHolder {
            val card = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_events_item, parent, false)
            return EventsAdapter.EventViewHolder(card, true)
        }

    }

}
