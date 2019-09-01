package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType
import de.tum.`in`.tumcampusapp.utils.Utils
import org.jetbrains.anko.textColor

class TicketAmountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    interface SelectTicketInterface {
        fun ticketAmountUpdated(ticketTypeId: Int, amount: Int)
    }

    private val minusButton: MaterialButton by lazy { itemView.findViewById<MaterialButton>(R.id.ticket_amount_minus) }
    private val plusButton: MaterialButton by lazy { itemView.findViewById<MaterialButton>(R.id.ticket_amount_plus) }
    private val currentAmount: TextView by lazy { itemView.findViewById<TextView>(R.id.current_ticket_amount) }
    private val ticketTypeName: TextView by lazy { itemView.findViewById<TextView>(R.id.ticket_type_name) }
    private val ticketPrice: TextView by lazy { itemView.findViewById<TextView>(R.id.price_per_ticket) }

    private var ticketAmount = 1
    private var ticketType = TicketType()
    private var ticketTypePos = 0
    private var remainingTickets = 0
    private var minAmount = 1
    private var maxAmount = 1

    fun bindToTicketType(ticketType: TicketType, position: Int) {
        this.ticketType = ticketType
        ticketTypePos = position

        minAmount = ticketType.paymentInfo.minTickets
        maxAmount = ticketType.paymentInfo.maxTickets

        if (position == 0) {
            ticketAmount = minAmount
            notifyActivity()
        } else {
            ticketAmount = 0
        }

        // init text views
        ticketTypeName.text = ticketType.description
        ticketPrice.text = itemView.resources.getString(R.string.price_per_ticket, Utils.formatPrice(ticketType.price))
        currentAmount.text = ticketAmount.toString()
        // init buttons
        plusButton.setOnClickListener { updateTicketAmount(true) }
        minusButton.setOnClickListener { updateTicketAmount(false) }

        // handle how many tickets are left
        remainingTickets = ticketType.contingent - ticketType.sold
        maxAmount = Math.min(maxAmount, remainingTickets)
        if (remainingTickets < minAmount) {
            ticketAmount = 0
            currentAmount.text = ticketAmount.toString()
            plusButton.isEnabled = false
            minusButton.isEnabled = false
            ticketTypeName.textColor = R.color.text_light_gray
        }
        updateButtonState()
    }

    /**
     * Updates the current ticket amount when the plus or minus button is clicked,
     * makes sure the min and max amount of tickets is adhered to (either 0 tickets or minAmount of tickets, nothing in between)
     * and the user doesn't select more tickets than are available
     */
    private fun updateTicketAmount(increase: Boolean) {
        if (increase) {
            if (ticketAmount == 0) {
                ticketAmount = minAmount
            } else {
                ticketAmount++
            }
        } else {
            if (ticketAmount == minAmount) {
                ticketAmount = 0
            } else {
                ticketAmount--
            }
        }
        currentAmount.text = ticketAmount.toString()
        updateButtonState()

        notifyActivity()
    }

    private fun notifyActivity() {
        if (itemView.context is SelectTicketInterface) {
            (itemView.context as SelectTicketInterface).ticketAmountUpdated(ticketTypePos, ticketAmount)
        } else {
            Utils.log("The context is not a SelectTicketInterface")
        }
    }

    private fun updateButtonState() {
        plusButton.isEnabled = ticketAmount != maxAmount
        minusButton.isEnabled = ticketAmount != 0
    }
}