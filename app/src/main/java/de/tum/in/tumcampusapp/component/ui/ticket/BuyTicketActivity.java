package de.tum.in.tumcampusapp.component.ui.ticket;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.tumonline.AccessTokenManager;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservationResponse;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuyTicketActivity extends BaseActivity {

    private EventsController eventsController;
    private int eventId;

    private Spinner ticketTypeSpinner;
    private ProgressBar reservationProgressBar;
    private Button paymentButton;

    private List<TicketType> ticketTypes;

    public BuyTicketActivity() {
        super(R.layout.activity_buy_ticket);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsController = new EventsController(this);

        eventId = getIntent().getIntExtra("eventID", 0);

        // get ticket type information from API
        TUMCabeClient.getInstance(getApplicationContext()).getTicketTypes(eventId,
                new Callback<List<TicketType>>(){

                    @Override
                    public void onResponse(Call<List<TicketType>> call, Response<List<TicketType>> response) {
                        ticketTypes = response.body();

                        // add found ticket types to database
                        eventsController.addTicketTypes(ticketTypes);

                        setupUi();
                    }

                    @Override
                    public void onFailure(Call<List<TicketType>> call, Throwable t) {
                        // if ticketTypes could not be retrieved from server, e.g. due to network problems
                        Utils.log(t);
                        Utils.showToast(getApplicationContext(), R.string.no_network_connection);
                        // go back to event details
                        Intent intent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                        intent.putExtra("event_id", eventId);
                        startActivity(intent);
                    }
                });
    }

    private void setupUi(){
        initEventTextViews();

        initializeTicketTypeSpinner();

        reservationProgressBar = findViewById(R.id.ticket_reservation_progressbar);
        reservationProgressBar.setVisibility(View.INVISIBLE);

        paymentButton = findViewById(R.id.paymentbutton);
        paymentButton.setOnClickListener(v -> {
            // Check if user is logged in and name and LRZ ID are available (needed to create ChatVerification)
            if (new AccessTokenManager(BuyTicketActivity.this).hasValidAccessToken() &&
                    Utils.getSetting(BuyTicketActivity.this, Const.LRZ_ID, "").length() > 0 &&
                    Utils.getSetting(BuyTicketActivity.this, Const.CHAT_ROOM_DISPLAY_NAME, "").length() > 0) {
                reserveTicket();
            } else {
                ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
                AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
                builder.setTitle(getString(R.string.sorry))
                        .setMessage(R.string.not_logged_in_message);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void initEventTextViews() {
        TextView eventView = findViewById(R.id.ticket_details_event);
        TextView locationView = findViewById(R.id.ticket_details_location);
        TextView dateView = findViewById(R.id.ticket_details_date);

        Event event = eventsController.getEventById(eventId);

        String eventString = event.getTitle();
        String locationString = event.getLocality();

        eventView.append(eventString);
        locationView.append(locationString);
        dateView.append(DateTimeUtils.INSTANCE.getDateTimeString(event.getStart()));
    }

    private void initializeTicketTypeSpinner() {
        ticketTypeSpinner = findViewById(R.id.ticket_type_spinner);
        ticketTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String ticketTypeName = (String) parent.getItemAtPosition(position);
                setTicketTypeInformation(ticketTypeName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing here for now
            }
        });

        ArrayList<String> ticketTypeNames = new ArrayList<>();
        for (TicketType ticketType : ticketTypes) {
            ticketTypeNames.add(ticketType.getDescription());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ticketTypeNames);
        ticketTypeSpinner.setAdapter(adapter);
    }

    private TicketType getTicketTypeForName(String ticketTypeName) {
        for (TicketType ticketType : ticketTypes) {
            if (ticketType.getDescription().equals(ticketTypeName)) {
                return ticketType;
            }
        }
        return null;
    }

    private void setTicketTypeInformation(String ticketTypeName) {
        TicketType ticketType = getTicketTypeForName(ticketTypeName);

        TextView priceView = findViewById(R.id.ticket_details_price);

        String priceString = ticketType.formatedPrice();

        priceView.setText(priceString);
    }

    private void reserveTicket() {
        TicketType ticketType = getTicketTypeForName((String) ticketTypeSpinner.getSelectedItem());
        if (ticketType == null) {
            Toast.makeText(getApplicationContext(), R.string.internal_error, Toast.LENGTH_LONG).show();
            return;
        }

        reservationProgressBar.setVisibility(View.VISIBLE);
        paymentButton.setEnabled(false);
        try {
            TUMCabeClient.getInstance(getApplicationContext()).reserveTicket(BuyTicketActivity.this, ticketType.getId(), new Callback<TicketReservationResponse>() {
                @Override
                public void onResponse(Call<TicketReservationResponse> call, Response<TicketReservationResponse> response) {
                    if (response.body().getError() != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BuyTicketActivity.this);
                        builder.setTitle(getString(R.string.sorry))
                                .setMessage(getString(R.string.ticket_contingent_exhausted));
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    } else {
                        reservationProgressBar.setVisibility(View.INVISIBLE);
                        paymentButton.setEnabled(true);

                        // Jump to the payment activity
                        Intent intent = new Intent(getApplicationContext(), StripePaymentActivity.class);
                        intent.putExtra("ticketPrice", ticketType.formatedPrice());
                        intent.putExtra("ticketType", ticketType.getId());
                        intent.putExtra("ticketHistory", response.body().getTicketHistory());
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailure(Call<TicketReservationResponse> call, Throwable t) {
                    t.printStackTrace();
                    reservationProgressBar.setVisibility(View.INVISIBLE);
                    paymentButton.setEnabled(true);
                    StripePaymentActivity.showError(BuyTicketActivity.this, getString(R.string.purchase_error_message));
                }
            });
        } catch (IOException exception) {
            reservationProgressBar.setVisibility(View.INVISIBLE);
            paymentButton.setEnabled(true);
            StripePaymentActivity.showError(BuyTicketActivity.this, getString(R.string.internal_error));
        }
    }


}

