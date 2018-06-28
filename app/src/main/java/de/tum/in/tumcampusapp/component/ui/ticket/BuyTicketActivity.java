package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;


public class BuyTicketActivity extends BaseActivity {

    private EventsController eventsController;

    public BuyTicketActivity() {
        super(R.layout.activity_buy_ticket);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventsController = new EventsController(this);

        TextView eventView = findViewById(R.id.ticket_details_event);
        TextView locationView = findViewById(R.id.ticket_details_location);
        TextView dateView = findViewById(R.id.ticket_details_date);
        TextView priceView = findViewById(R.id.ticket_details_price);
        TextView ticketTypeView = findViewById(R.id.ticket_details_ticket_type);
        Button paymentButton = findViewById(R.id.paymentbutton);

        int eventId = getIntent().getIntExtra("eventID", 0);

        Event event = eventsController.getEventById(eventId);
        ArrayList<TicketType> ticketTypes = (ArrayList<TicketType>) eventsController.getTicketTypesByEventId(eventId);
        TicketType chosenTicketType = null;

        if (ticketTypes != null){
            Spinner ticketTypeSpinner = findViewById(R.id.ticket_type_spinner);

            ArrayAdapter adapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, ticketTypes);
            ticketTypeSpinner.setAdapter(adapter);
        }else{
            // TODO: no internet connection, what to do then
        }

        // TODO: get ticket type from server here as soon as the backend implementation is ready
        // Create ticket locally for now for testing purposes
        String eventString = event.getTitle();
        String locationString = event.getLocality();
        String dateString = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.GERMANY).
                format(event.getDate());
        String priceString = new DecimalFormat("#.00").format(chosenTicketType.getPrice())
                + " €";
        String ticketTypeString = chosenTicketType.getDescription();

        eventView.append(eventString);
        locationView.append(locationString);
        dateView.append(dateString);
        priceView.append(priceString);
        ticketTypeView.append(ticketTypeString);


        paymentButton.setOnClickListener(v -> {
            //TODO: jump to next activity, the activity to pay by Strip
            Intent intent = new Intent(getApplicationContext(), TicketPaymentSelectActivity.class);
            startActivity(intent);
        });
    }

}

