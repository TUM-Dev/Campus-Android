package de.tum.in.tumcampusapp.component.ui.ticket;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;

public class BuyTicketActivity extends BaseActivity {
    private TextView paymentDetailsTextView;
    private Button paymentButton;

    public BuyTicketActivity() {
        super(R.layout.activity_buy_ticket);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        paymentDetailsTextView = (TextView) findViewById(R.id.ticketdetail);
        paymentButton = (Button) findViewById(R.id.paymentbutton);

        //Get data from Api backend, now it is mock up data
        Ticket ticket = TicketsController.getTickets();

        //load eventdetail
        String paymentdetail = "1× Ticket for" + "\n" + "         Event Name:" + ticket.getEvent().getTitle() + "\n" + "         Location:" + ticket.getEvent().getLocality() +
                "\n" + "         Date:" + "\n" + ticket.getEvent().getDate() + "\n" + "         Price" + ticket.getType().getPrice();
        paymentDetailsTextView.setText(paymentdetail);
        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: jump to next activity, the activity to pay by Strip
            }
        });
    }

}

