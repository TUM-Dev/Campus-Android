package de.tum.in.tumcampusapp.component.ui.ticket;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;

public class BuyTicketActivity extends BaseActivity {

    public BuyTicketActivity() {
        super(R.layout.activity_buy_ticket);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView eventView = findViewById(R.id.ticket_details_event);
        TextView locationView = findViewById(R.id.ticket_details_location);
        TextView dateView = findViewById(R.id.ticket_details_date);
        TextView priceView = findViewById(R.id.ticket_details_price);
        TextView ticketTypeView = findViewById(R.id.ticket_details_ticket_type);
        Button paymentButton = findViewById(R.id.paymentbutton);

        // TODO: Get data from Api backend, now it is mock up data
        Ticket ticket = TicketsController.getTickets();

        String eventString = ticket.getEvent().getTitle();
        String locationString = ticket.getEvent().getLocality();
        String dateString = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.GERMANY).
                format(ticket.getEvent().getDate());
        String priceString = new DecimalFormat("#.00").format(ticket.getType().getPrice())
                + " €";
        String ticketTypeString = ticket.getType().getDescription();

        eventView.append(eventString);
        locationView.append(locationString);
        dateView.append(dateString);
        priceView.append(priceString);
        ticketTypeView.append(ticketTypeString);

        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: jump to next activity, the activity to pay by Strip
            }
        });
    }

}

