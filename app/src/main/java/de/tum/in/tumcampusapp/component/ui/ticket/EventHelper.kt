package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.view.View
import android.widget.TextView
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketStatus

/**
 * Logic associated with the buy button that is needed in the EventDetailsFragment
 * and the KinoDetailsFragment.
 */
class EventHelper {
    companion object {
        fun showRemainingTickets(
            status: TicketStatus?,
            isEventBooked: Boolean,
            isEventImminent: Boolean,
            buyTicketButton: View,
            remainingTicketsContainer: View,
            remainingTicketsTextView: TextView,
            noTicketsMessage: String
        ) { }
    }
}
