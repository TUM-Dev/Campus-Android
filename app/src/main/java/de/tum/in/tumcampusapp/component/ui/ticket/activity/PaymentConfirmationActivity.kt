package de.tum.`in`.tumcampusapp.component.ui.ticket.activity

import android.content.Intent
import android.os.Bundle
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_payment_confirmation.*

class PaymentConfirmationActivity : BaseActivity(R.layout.activity_payment_confirmation) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventId = intent.getIntExtra("eventID", -1)
        if (eventId == -1) {
            finish()
            return
        }

        showTicketButton.setOnClickListener {
            val intent = Intent(this, ShowTicketActivity::class.java)
            intent.putExtra("eventID", eventId)
            startActivity(intent);
        }

        doneButton.setOnClickListener {
            val intent = Intent(this, EventsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
            finish()
        }
    }

}