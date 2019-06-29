package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.BuyTicketActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketStatus
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import org.joda.time.DateTime
import java.util.*

/**
 * Logic associated with the buy button that is needed in the EventDetailsFragment
 * and the KinoDetailsFragment.
 */
class EventHelper {
    companion object {
        fun buyTicket(event: Event, buyButton: View, context: Context?) {
            if (context == null) {
                return
            }

            if (isEventImminent(event)) {
                showEventImminentDialog(context)
                buyButton.visibility = View.GONE
                return
            }

            val lrzId = Utils.getSetting(context, Const.LRZ_ID, "")
            val chatRoomName = Utils.getSetting(context, Const.CHAT_ROOM_DISPLAY_NAME, "")
            val isLoggedIn = AccessTokenManager.hasValidAccessToken(context)

            if (!isLoggedIn || lrzId.isEmpty() || chatRoomName.isEmpty()) {
                val dialog = AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(R.string.not_logged_in_error)
                        .setPositiveButton(R.string.ok, null)
                        .create()

                dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
                dialog.show()
                return
            }

            val intent = Intent(context, BuyTicketActivity::class.java).apply {
                putExtra(Const.KEY_EVENT_ID, event.id)
            }
            context.startActivity(intent)
        }

        private fun showEventImminentDialog(context: Context) {
            val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(R.string.event_imminent_error)
                    .setPositiveButton(R.string.ok, null)
                    .create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            dialog.show()
        }

        /**
         * Checks if the event starts less than 4 hours from now.
         * (-> user won't be able to buy tickets anymore)
         */
        fun isEventImminent(event: Event): Boolean {
            val eventStart = DateTime(event.startTime)
            return DateTime.now().isAfter(eventStart.minusHours(4))
        }

        fun showRemainingTickets(
                status: TicketStatus?,
                isEventBooked: Boolean,
                isEventImminent: Boolean,
                buyTicketButton: View,
                remainingTicketsContainer: View,
                remainingTicketsTextView: TextView,
                noTicketsMessage: String) {

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