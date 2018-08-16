package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.tumonline.AccessTokenManager;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatVerification;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservation;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservationResponse;
import de.tum.in.tumcampusapp.utils.Const;
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
        eventId = getIntent().getIntExtra(Const.KEY_EVENT_ID, 0);

        // Get ticket type information from API
        TUMCabeClient
                .getInstance(this)
                .fetchTicketTypes(eventId, new Callback<List<TicketType>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<TicketType>> call,
                                           @NonNull Response<List<TicketType>> response) {
                        List<TicketType> results = response.body();
                        if (results != null) {
                            handleTicketTypesDownloadSuccess(results);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<TicketType>> call, @NonNull Throwable t) {
                        Utils.log(t);
                        Utils.showToast(BuyTicketActivity.this, R.string.error_something_wrong);
                        finish();
                    }
                });
    }

    private void handleTicketTypesDownloadSuccess(@NonNull List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
        eventsController.addTicketTypes(ticketTypes);
        setupUi();
    }

    private void setupUi() {
        initEventTextViews();
        initTicketTypeSpinner();

        reservationProgressBar = findViewById(R.id.paymentProgressBar);
        reservationProgressBar.setVisibility(View.INVISIBLE);

        paymentButton = findViewById(R.id.paymentButton);
        paymentButton.setOnClickListener(v -> {
            String lrzId = Utils.getSetting(this, Const.LRZ_ID, "");
            String chatRoomName = Utils.getSetting(this, Const.CHAT_ROOM_DISPLAY_NAME, "");
            boolean isLoggedIn = AccessTokenManager.hasValidAccessToken(this);

            // Check if user is logged in and name and LRZ ID are available
            if (isLoggedIn && !lrzId.isEmpty() && !chatRoomName.isEmpty()) {
                reserveTicket();
            } else {
                //ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.sorry))
                        .setMessage(R.string.not_logged_in_message) // TODO
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
        });
    }

    private void initEventTextViews() {
        TextView eventView = findViewById(R.id.ticket_details_event);
        TextView locationView = findViewById(R.id.ticket_details_location);
        TextView dateView = findViewById(R.id.ticket_details_date);

        Event event = eventsController.getEventById(eventId);

        eventView.setText(event.getTitle());
        locationView.setText(event.getLocality());

        String formattedStartTime = event.getFormattedStartDateTime(this);
        dateView.setText(formattedStartTime);
    }

    private void initTicketTypeSpinner() {
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

        ArrayAdapter adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, ticketTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        TextView priceView = findViewById(R.id.ticket_details_price);
        TicketType ticketType = getTicketTypeForName(ticketTypeName);
        String priceString = ticketType == null ? getString(R.string.not_valid) : ticketType.getFormattedPrice();

        priceView.setText(priceString);
    }

    private void reserveTicket() {
        TicketType ticketType = getTicketTypeForName((String) ticketTypeSpinner.getSelectedItem());
        if (ticketType == null) {
            Utils.showToast(this, R.string.internal_error);
            return;
        }

        reservationProgressBar.setVisibility(View.VISIBLE);
        paymentButton.setEnabled(false);

        int ticketTypeId = ticketType.getId();
        TicketReservation reservation = new TicketReservation(ticketTypeId);

        ChatVerification verification;

        try {
            verification = ChatVerification.Companion
                    .createChatVerification(this, reservation);
        } catch (IOException e) {
            handleTicketReservationFailure(R.string.internal_error);
            return;
        }

        TUMCabeClient
                .getInstance(this)
                .reserveTicket(this, verification, new Callback<TicketReservationResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TicketReservationResponse> call,
                                           @NonNull Response<TicketReservationResponse> response) {
                        // ResponseBody can be null if the user has already bought a ticket
                        // but has not fetched it from the server yet
                        TicketReservationResponse reservationResponse = response.body();
                        if (response.isSuccessful() && reservationResponse != null) {
                            handleTicketReservationSuccess(ticketType, reservationResponse);
                        } else {
                            handleTicketNotFetched();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TicketReservationResponse> call,
                                          @NonNull Throwable t) {
                        Utils.log(t);
                        handleTicketReservationFailure(R.string.error_something_wrong);
                    }
                });
    }

    private void handleTicketReservationSuccess(TicketType ticketType,
                                                TicketReservationResponse response) {
        reservationProgressBar.setVisibility(View.INVISIBLE);
        paymentButton.setEnabled(true);

        // Jump to the payment activity
        Intent intent = new Intent(this, StripePaymentActivity.class);
        intent.putExtra("ticketPrice", ticketType.getFormattedPrice());
        intent.putExtra("ticketType", ticketType.getId());
        intent.putExtra("ticketHistory", response.getTicketHistory());
        startActivity(intent);
    }

    private void handleTicketNotFetched() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.sorry))
                .setMessage(getString(R.string.ticket_not_fetched))
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    reservationProgressBar.setVisibility(View.INVISIBLE);
                    paymentButton.setEnabled(true);
                })
                .show();
    }

    private void handleTicketReservationFailure(int messageResId) {
        reservationProgressBar.setVisibility(View.INVISIBLE);
        paymentButton.setEnabled(true);
        Utils.showToast(this, messageResId);
    }

}

