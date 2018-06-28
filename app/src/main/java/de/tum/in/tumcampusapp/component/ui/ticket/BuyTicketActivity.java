package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;

public class BuyTicketActivity extends BaseActivity {

    private EventsController eventsController;
    private int eventId;

    private Spinner ticketTypeSpinner;

    private List<TicketType> ticketTypes;

    public BuyTicketActivity() {
        super(R.layout.activity_buy_ticket);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsController = new EventsController(this);

        eventId = getIntent().getIntExtra("eventID", 0);

        initEventTextViews();

        // get ticket type information from API
        Thread thread = new Thread(){
            public void run(){
                ticketTypes = eventsController.getTicketTypesByEventId(eventId);
            }
        };
        thread.start();
        try {
            // TODO: insert something to visualize that the system is loading if necessary
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // TODO: remove this (only for testing purposes)
        ticketTypes.add(new TicketType(42,4.2,"Test Ticket"));

        ticketTypeSpinner = findViewById(R.id.ticket_type_spinner);
        ticketTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String ticketTypeName = (String)parent.getItemAtPosition(position);
                setTicketTypeInformation(ticketTypeName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO: what to do here?
            }
        });

        initializeTicketTypeSpinner();

        // TODO: check if this is necessary; OnItemSelected already called initially
        setTicketTypeInformation((String)ticketTypeSpinner.getSelectedItem());

        Button paymentButton = findViewById(R.id.paymentbutton);
        paymentButton.setOnClickListener(v -> {
            // Jump to the payment activity
            Intent intent = new Intent(getApplicationContext(), TicketPaymentSelectActivity.class);
            intent.putExtra("ticketTypeId", getTicketTypeForName(
                    (String)ticketTypeSpinner.getSelectedItem()).getId());
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });
    }

    private void initEventTextViews() {
        TextView eventView = findViewById(R.id.ticket_details_event);
        TextView locationView = findViewById(R.id.ticket_details_location);
        TextView dateView = findViewById(R.id.ticket_details_date);

        Event event = eventsController.getEventById(eventId);

        String eventString = event.getTitle();
        String locationString = event.getLocality();
        String dateString = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.GERMANY).
                format(event.getDate());
        eventView.append(eventString);
        locationView.append(locationString);
        dateView.append(dateString);
    }

    private void initializeTicketTypeSpinner() {
        ArrayList<String> ticketTypeNames = new ArrayList<>();
        for (TicketType ticketType : ticketTypes){
            ticketTypeNames.add(ticketType.getDescription());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ticketTypeNames);
        ticketTypeSpinner.setAdapter(adapter);
    }

    private TicketType getTicketTypeForName(String ticketTypeName){
        for(TicketType ticketType : ticketTypes){
            if (ticketType.getDescription().equals(ticketTypeName)){
                return ticketType;
            }
        }
        return null;
    }

    private void setTicketTypeInformation(String ticketTypeName){
        TicketType ticketType = getTicketTypeForName(ticketTypeName);

        TextView priceView = findViewById(R.id.ticket_details_price);
        TextView ticketTypeView = findViewById(R.id.ticket_details_ticket_type);

        String priceString = new DecimalFormat("#.00").format(ticketType.getPrice())
                + " €";
        String ticketTypeString = ticketType.getDescription();

        priceView.setText(priceString);
        ticketTypeView.setText(ticketTypeString);
    }


}

