package de.tum.`in`.tumcampusapp.component.ui.ticket.activity

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.MenuItem
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.utils.Const
import kotlinx.android.synthetic.main.activity_payment_confirmation.*
import java.util.*
import kotlin.concurrent.schedule

class PaymentConfirmationActivity : BaseActivity(R.layout.activity_payment_confirmation) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventId = intent.getIntExtra(Const.KEY_EVENT_ID, -1)
        val ticketAmount = intent.getIntExtra(Const.KEY_TICKET_AMOUNT, 1)
        if (eventId == -1) {
            finish()
            return
        }

        bodyTextView.text = resources.getQuantityString(R.plurals.tickets_saved_in_app, ticketAmount)
        showTicketButton.text = resources.getQuantityString(R.plurals.show_tickets, ticketAmount)

        showTicketButton.setOnClickListener {
            val intent = Intent(this, ShowTicketActivity::class.java)
            intent.putExtra(Const.KEY_EVENT_ID, eventId)
            startActivity(intent)
        }

        doneButton.setOnClickListener {
            val intent = Intent(this, EventsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
            finish()
        }

        runCheckmarkAnimation()
    }

    private fun runCheckmarkAnimation() {
        Timer().schedule(200) {
            runOnUiThread {
                val animatedCheckmark = imageView.drawable as? Animatable
                animatedCheckmark?.start()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                handleOnBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        handleOnBackPressed()
    }

    private fun handleOnBackPressed() {
        // Go back to events and finish this activity to prevent the user from purchasing
        // another ticket.
        val intent = Intent(this, EventsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}
