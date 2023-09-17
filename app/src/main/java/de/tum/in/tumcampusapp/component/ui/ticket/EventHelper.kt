package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketStatus
import java.util.Locale

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
        ) {
            if (isEventImminent) {
                buyTicketButton.isVisible = isEventBooked
                remainingTicketsContainer.isVisible = false
            } else {
                if (status == null || status.isEventWithoutTickets()) {
                    buyTicketButton.isVisible = isEventBooked
                    remainingTicketsContainer.isVisible = false
                } else if (status.ticketsStillAvailable()) {
                    buyTicketButton.isVisible = true
                    remainingTicketsContainer.isVisible = true
                    remainingTicketsTextView.text = String.format(Locale.getDefault(), "%d", status.getRemainingTicketCount())
                } else {
                    buyTicketButton.isVisible = isEventBooked
                    remainingTicketsContainer.isVisible = true
                    remainingTicketsTextView.text = noTicketsMessage
                }
            }
        }
    }
}
