package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration;
import de.tum.in.tumcampusapp.component.ui.ticket.TicketAmountViewHolder;
import de.tum.in.tumcampusapp.component.ui.ticket.adapter.TicketAmountAdapter;
import de.tum.in.tumcampusapp.component.ui.ticket.di.TicketsModule;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservation;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservationResponse;
import de.tum.in.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository;
import de.tum.in.tumcampusapp.component.ui.ticket.repository.TicketsRemoteRepository;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity shows an overview of the available tickets and a selection of all ticket types
 * Directs the user to the PaymentConfirmationActivity or back to EventDetailsActivity
 */
public class BuyTicketActivity extends BaseActivity implements TicketAmountViewHolder.SelectTicketInterface {

    private int eventId;

    private FrameLayout loadingLayout;
    private Button paymentButton;

    private TextView totalPriceView;

    private List<TicketType> ticketTypes;
    private Integer[] currentTicketAmounts;

    @Inject
    TicketsRemoteRepository ticketsRemoteRepo;

    @Inject
    EventsLocalRepository eventsLocalRepo;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public BuyTicketActivity() {
        super(R.layout.activity_buy_ticket);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventId = getIntent().getIntExtra(Const.KEY_EVENT_ID, 0);

        totalPriceView = findViewById(R.id.ticket_total_price);
        totalPriceView.setText(Utils.formatPrice(0));

        loadingLayout = findViewById(R.id.loading_layout);

        getInjector().ticketsComponent()
                .eventId(eventId)
                .build()
                .inject(this);

        // Get ticket type information from API
        Disposable disposable = ticketsRemoteRepo.fetchTicketTypesForEvent(eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(Utils::log)
                .subscribe(this::handleTicketTypesDownloadSuccess, throwable -> {
                    Utils.showToast(BuyTicketActivity.this, R.string.error_something_wrong);
                    finish();
                });
         compositeDisposable.add(disposable);
    }

    private void handleTicketTypesDownloadSuccess(@NonNull List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;

        currentTicketAmounts = new Integer[ticketTypes.size()];
        setupUi();
    }

    private void setupUi() {
        initEventTextViews();
        initTicketAmount();

        loadingLayout.setVisibility(View.GONE);

        paymentButton = findViewById(R.id.paymentButton);
        paymentButton.setOnClickListener(v -> reserveTicket());
    }

    private void initTicketAmount() {
        RecyclerView ticketAmounts = findViewById(R.id.ticket_amounts);
        ticketAmounts.setLayoutManager(new LinearLayoutManager(this));
        ticketAmounts.setHasFixedSize(true);
        ticketAmounts.setAdapter(new TicketAmountAdapter(ticketTypes));
        ticketAmounts.setNestedScrollingEnabled(false);
        int spacing = Math.round(getResources().getDimension(R.dimen.material_small_padding));
        ticketAmounts.addItemDecoration(new EqualSpacingItemDecoration(spacing));
    }

    @Override
    public void ticketAmountUpdated(int ticketTypePosition, int amount) {
        currentTicketAmounts[ticketTypePosition] = amount;
        totalPriceView.setText(Utils.formatPrice(getTotalPrice()));
    }

    private int getTotalPrice() {
        if (currentTicketAmounts == null) {
            Utils.log("currentTicketAmounts not initialized");
            return 0;
        }
        int sum = 0;
        for (int i = 0; i < ticketTypes.size(); i++) {
            Integer count = currentTicketAmounts[i];
            if (count != null && count > 0) {
                int pricePerTicket = ticketTypes.get(i)
                                                .getPrice();
                sum += pricePerTicket * count;
            }
        }
        return sum;
    }

    private int getTotalTickets() {
        if (currentTicketAmounts == null) {
            Utils.log("currentTicketAmounts not initialized");
            return 0;
        }
        int sum = 0;
        for (int i = 0; i < ticketTypes.size(); i++) {
            Integer count = currentTicketAmounts[i];
            if (count != null && count > 0) {
                sum += count;
            }
        }
        return sum;
    }

    private void showError(int title, int message) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.ok, null)
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

        Event event = eventsLocalRepo.getEventById(eventId);

        eventView.setText(event.getTitle());
        locationView.setText(event.getLocality());

        String formattedStartTime = event.getFormattedStartDateTime(this);
        dateView.setText(formattedStartTime);
    }

    private Integer[] getTicketTypeIds() {
        Integer[] ids = new Integer[ticketTypes.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = ticketTypes.get(i)
                                .getId();
        }
        return ids;
    }

    private boolean zeroTicketsSelected() {
        return getTotalTickets() == 0;
    }

    private void reserveTicket() {

        if (zeroTicketsSelected()) {
            showError(R.string.error_no_ticket_selected, R.string.error_message_select_at_least_one_ticket);
            return;
        }

        /* don't allow user to click anything */
        loadingLayout.setVisibility(View.VISIBLE);
        TransitionManager.beginDelayedTransition(loadingLayout);
        paymentButton.setEnabled(false);

        TicketReservation reservation = new TicketReservation(getTicketTypeIds(), currentTicketAmounts);

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
                            handleTicketReservationSuccess(reservationResponse);
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

    private void handleTicketReservationSuccess(TicketReservationResponse response) {
        loadingLayout.setVisibility(View.GONE);
        TransitionManager.beginDelayedTransition(loadingLayout);
        paymentButton.setEnabled(true);

        Intent intent = new Intent(this, StripePaymentActivity.class);
        intent.putExtra(Const.KEY_TICKET_PRICE, Utils.formatPrice(getTotalPrice()));
        intent.putIntegerArrayListExtra(Const.KEY_TICKET_IDS, response.getTicketIds());
        intent.putExtra(Const.KEY_TERMS_LINK, ticketTypes.get(0)
                                                         .getPaymentInfo()
                                                         .getTermsLink());
        intent.putExtra(Const.KEY_STRIPE_API_PUBLISHABLE_KEY, ticketTypes.get(0)
                                                                         .getPaymentInfo()
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

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }
}

