package de.tum.`in`.tumcampusapp.component.ui.ticket.activity

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.TicketAmountViewHolder

/**
 * This activity shows an overview of the available tickets and a selection of all ticket type.
 * It directs the user to the PaymentConfirmationActivity or back to EventDetailsActivity
 */
class BuyTicketActivity : BaseActivity(R.layout.activity_buy_ticket), TicketAmountViewHolder.SelectTicketInterface {

    override fun ticketAmountUpdated(ticketTypeId: Int, amount: Int) {
    }
}
