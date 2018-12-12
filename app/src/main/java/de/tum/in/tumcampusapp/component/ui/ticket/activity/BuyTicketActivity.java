package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Payment;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservation;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservationResponse;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity shows an overview of the available tickets and a selection of all ticket types
 * Directs the user to the PaymentConfirmationActivity or back to EventDetailsActivity
 */
public class BuyTicketActivity extends BaseActivity {

    private EventsController eventsController;
    private int eventId;

    private Spinner ticketTypeSpinner;
    private FrameLayout loadingLayout;
    private Button paymentButton;

    private TextView ticketAmountTextView;
    private int currentTicketAmount;
    private Button minusButton, plusButton;

    private TextView priceView;

    private List<TicketType> ticketTypes;
    private int ticketTypeSelected;

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
        ticketTypes.get(0).getPaymentInfo().setMaxTickets(5); // TODO(bronger) remove, this is just for testing
        eventsController.addTicketTypes(ticketTypes);
        setupUi();
    }

    private void setupUi() {
        initEventTextViews();
        initTicketTypeSpinner();
        initTicketAmount();

        loadingLayout = findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.GONE);

        paymentButton = findViewById(R.id.paymentButton);
        paymentButton.setOnClickListener(v -> reserveTicket());
    }

    private void initTicketAmount() {
        priceView = findViewById(R.id.ticket_details_price);
        ticketAmountTextView = findViewById(R.id.ticket_amount_number_text_view);
        minusButton = findViewById(R.id.ticket_amount_minus);
        plusButton = findViewById(R.id.ticket_amount_plus);
        currentTicketAmount = 1;
        updateTicketAmount();

        minusButton.setOnClickListener(view -> {
            currentTicketAmount--;
            updateTicketAmount();
        });
        plusButton.setOnClickListener(view -> {
            currentTicketAmount++;
            updateTicketAmount();
        });
    }

    private void updateTicketAmount() {
        Payment paymentInfo = ticketTypes.get(ticketTypeSelected)
                                         .getPaymentInfo();
        int maxAmount = paymentInfo.getMaxTickets();
        int minAmount = paymentInfo.getMinTickets();

        if (currentTicketAmount > maxAmount) {
            currentTicketAmount = maxAmount;
            showTicketAmountInformation(paymentInfo);
        }
        if (currentTicketAmount < minAmount) {
            currentTicketAmount = minAmount;
            showTicketAmountInformation(paymentInfo);
        }

        // Don't show buttons for more/less tickets if only a fixed amount can be bought
        plusButton.setVisibility(minAmount == maxAmount ? View.GONE : View.VISIBLE);
        minusButton.setVisibility(minAmount == maxAmount ? View.GONE : View.VISIBLE);
        plusButton.setEnabled(currentTicketAmount != maxAmount);
        minusButton.setEnabled(currentTicketAmount != minAmount);

        ticketAmountTextView.setText(currentTicketAmount + "");
        updatePrices();
    }

    private void showTicketAmountInformation(Payment paymentInfo) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.ticket_amount_error_title)
                .setMessage(getString(
                        R.string.ticket_amount_information,
                        paymentInfo.getMinTickets(),
                        paymentInfo.getMaxTickets(), ticketTypes.get(ticketTypeSelected).getDescription()))
                .setPositiveButton(R.string.ok, null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow()
                  .setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }
        dialog.show();
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
        ticketTypeSelected = 0;
        ticketTypeSpinner = findViewById(R.id.ticket_type_spinner);
        ticketTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ticketTypeSelected = position;
                updateTicketAmount();
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, ticketTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ticketTypeSpinner.setAdapter(adapter);
    }

    private TicketType getTicketTypeForName(String ticketTypeName) {
        for (TicketType ticketType : ticketTypes) {
            if (ticketType.getDescription()
                          .equals(ticketTypeName)) {
                return ticketType;
            }
        }
        return null;
    }

    private void updatePrices() {
        TicketType ticketType = ticketTypes.get(ticketTypeSelected);
        String priceString = ticketType.formatPrice(ticketType.getPrice() * currentTicketAmount);
        if (currentTicketAmount == 1) {
            priceView.setText(priceString);
        } else {
            String perTicketPrice = ticketType.formatPrice(ticketType.getPrice());
            priceView.setText(getString(R.string.ticket_price_multiple_tickets, priceString, perTicketPrice));
        }

    }

    private void reserveTicket() {
        TicketType ticketType = getTicketTypeForName((String) ticketTypeSpinner.getSelectedItem());
        if (ticketType == null) {
            Utils.showToast(this, R.string.internal_error);
            return;
        }

        loadingLayout.setVisibility(View.VISIBLE);
        TransitionManager.beginDelayedTransition(loadingLayout);
        paymentButton.setEnabled(false);
        plusButton.setEnabled(false);
        minusButton.setEnabled(false);

        int ticketTypeId = ticketType.getId();
        TicketReservation reservation = new TicketReservation(ticketTypeId, currentTicketAmount);

        TUMCabeVerification verification = TUMCabeVerification.create(this, reservation);
        if (verification == null) {
            handleTicketReservationFailure(R.string.internal_error);
            return;
        }

        TUMCabeClient
                .getInstance(this)
                .reserveTicket(verification, new Callback<TicketReservationResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TicketReservationResponse> call,
                                           @NonNull Response<TicketReservationResponse> response) {
                        // ResponseBody can be null if the user has already bought a ticket
                        // but has not fetched it from the server yet
                        TicketReservationResponse reservationResponse = response.body();
                        if (response.isSuccessful()
                            && reservationResponse != null
                            && reservationResponse.getError() == null) {
                            handleTicketReservationSuccess(ticketType, reservationResponse);
                        } else {
                            if (reservationResponse == null || !response.isSuccessful()) {
                                handleTicketNotReserved();
                            } else {
                                handleTicketReservationFailure(R.string.event_imminent_error);
                                finish();
                            }
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
        loadingLayout.setVisibility(View.GONE);
        TransitionManager.beginDelayedTransition(loadingLayout);

        Intent intent = new Intent(this, StripePaymentActivity.class);
        intent.putExtra(Const.KEY_TICKET_PRICE, ticketType.formatPrice(currentTicketAmount * ticketType.getPrice()));
        intent.putIntegerArrayListExtra(Const.KEY_TICKET_IDS, response.getTicketIds());
        intent.putExtra(Const.KEY_TERMS_LINK, ticketType.getPaymentInfo()
                                                        .getTermsLink());
        intent.putExtra(Const.KEY_STRIPE_API_PUBLISHABLE_KEY, ticketType.getPaymentInfo()
                                                                        .getStripePublicKey());
        startActivity(intent);
    }

    private void handleTicketNotReserved() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(getString(R.string.ticket_not_fetched))
                .setPositiveButton(R.string.ok, (dialogInterface, which) -> {
                    loadingLayout.setVisibility(View.GONE);
                    TransitionManager.beginDelayedTransition(loadingLayout);
                    paymentButton.setEnabled(true);
                    updateTicketAmount();
                })
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow()
                  .setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

    private void handleTicketReservationFailure(int messageResId) {
        loadingLayout.setVisibility(View.GONE);
        TransitionManager.beginDelayedTransition(loadingLayout);

        paymentButton.setEnabled(true);
        Utils.showToast(this, messageResId);
    }

}

