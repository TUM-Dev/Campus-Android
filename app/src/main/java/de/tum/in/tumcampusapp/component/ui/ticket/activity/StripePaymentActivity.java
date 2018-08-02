package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.stripe.android.CustomerSession;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.model.Source;
import com.stripe.android.model.SourceCardData;
import com.stripe.android.view.PaymentMethodsActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.TicketEphemeralKeyProvider;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketSuccessResponse;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StripePaymentActivity extends BaseActivity {

    private AppCompatButton buyButton;

    private TextView selectMethodButton;
    private LinearLayout selectedMethodLayout;
    private TextView selectedMethodTextView;
    private TextView selectedMethodCardTypeTextView;

    private ProgressBar progressBar;
    private EditText cardholderEditText;

    private PaymentSession paymentSession;
    private Boolean setSource = false; // Indicates whether the source has already been loaded from Stripe Server
    private int ticketHistory; // Ticket ID, since the ticket was reserved in the prior activity and
                               // we need the ID to init the purchase
    private String price = ""; // ticket price is only used to display in textView -> String

    public StripePaymentActivity() {
        super(R.layout.activity_payment_stripe);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        price = getIntent().getStringExtra("ticketPrice");
        ticketHistory = getIntent().getIntExtra("ticketHistory", -1);

        if (ticketHistory < 0 || price.isEmpty()) {
            Utils.showToast(getApplicationContext(), R.string.internal_error);
            finish();
            return;
        }

        initSubviews();
        initStripeSession();
    }

    private void initSubviews() {
        cardholderEditText = findViewById(R.id.cardholder_edit_text);

        progressBar = findViewById(R.id.stripe_purchase_progress);
        progressBar.setVisibility(View.INVISIBLE);

        selectMethodButton = findViewById(R.id.select_payment_method_button);
        selectMethodButton.setOnClickListener(view -> paymentSession.presentPaymentMethodSelection());
        selectMethodButton.setEnabled(false);

        selectedMethodLayout = findViewById(R.id.selected_payment_method_container);
        selectedMethodTextView = findViewById(R.id.selected_payment_method_text_view);
        selectedMethodCardTypeTextView = findViewById(R.id.selected_payment_method_type_text_view);

        ImageButton changeMethodButton = findViewById(R.id.change_payment_method_button);
        changeMethodButton.setOnClickListener(v -> paymentSession.presentPaymentMethodSelection());

        String buyButtonString = getString(R.string.buy_format_string, price);
        buyButton = findViewById(R.id.complete_purchase_button);
        buyButton.setText(buyButtonString);

        buyButton.setOnClickListener(v -> requestTicket());
        buyButton.setEnabled(false); // disabled until customer and payment session have been loaded
    }

    private void requestTicket() {
        String cardholder = cardholderEditText.getText().toString();
        if (cardholder.isEmpty()) {
            Utils.showToast(this, R.string.empty_cardholder_message);
            return;
        }

        if (!setSource) {
            // No payment source selected yet
            Utils.showToast(this, R.string.card_data_invalid);
            return;
        }

        setPurchaseRequestLoading(true);

        try {
            String paymentMethodId =
                    paymentSession.getPaymentSessionData().getSelectedPaymentMethodId();

            TUMCabeClient
                    .getInstance(this)
                    .purchaseTicketStripe(this, ticketHistory,
                            paymentMethodId, cardholder, new Callback<Ticket>() {
                                @Override
                                public void onResponse(@NonNull Call<Ticket> call,
                                                       @NonNull Response<Ticket> response) {
                                    EventsController ec = new EventsController(
                                            StripePaymentActivity.this);
                                    List<Ticket> ticketList = new ArrayList<>();
                                    ticketList.add(response.body());
                                    ec.replaceTickets(ticketList);
                                    finishLoadingPurchaseRequestSuccess(response.body());
                                }

                                @Override
                                public void onFailure(@NonNull Call<Ticket> call, @NonNull Throwable t) {
                                    Utils.log(t);
                                    finishLoadingPurchaseRequestError(getString(R.string.ticket_retrieval_error));
                                }
                            });
        } catch (IOException exception) {
            Utils.log(exception);
            finishLoadingPurchaseRequestError(getString(R.string.purchase_error_message));
        }
    }

    private void setPurchaseRequestLoading(boolean showLoading) {
        progressBar.setVisibility(showLoading ? View.VISIBLE : View.GONE);
        buyButton.setVisibility(showLoading ? View.GONE : View.VISIBLE);
    }

    private void finishLoadingPurchaseRequestSuccess(Ticket ticket) {
        setPurchaseRequestLoading(false);

        Intent intent = new Intent(this, PaymentConfirmationActivity.class);
        intent.putExtra("eventID", ticket.getEventId());
        startActivity(intent);
    }

    private void finishLoadingPurchaseRequestError(String error) {
        setPurchaseRequestLoading(false);
        showError(error);
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    /* On return of the credit card selection */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Data might be null if user tapped Back button from Credit Card selection
        if (data != null) {
            String selectedSource = data.getStringExtra(PaymentMethodsActivity.EXTRA_SELECTED_PAYMENT);
            Source source = Source.fromString(selectedSource);
            // Note: it isn't possible for a null or non-card source to be returned.
            if (source != null && Source.CARD.equals(source.getType())) {
                SourceCardData cardData = (SourceCardData) source.getSourceTypeModel();
                selectMethodButton.setVisibility(View.GONE);
                selectedMethodLayout.setVisibility(View.VISIBLE);
                selectedMethodTextView.setText(buildCardString(cardData));
                selectedMethodCardTypeTextView.setText(cardData.getBrand());
                setSource = true;
            }
        }
    }

    private void initStripeSession() {
        PaymentConfiguration.init(Const.STRIPE_API_PUBLISHABLE_KEY);
        initCustomerSession();
    }

    private void initCustomerSession() {
        CustomerSession.initCustomerSession(new TicketEphemeralKeyProvider(string -> {
            if (string.startsWith("Error: ")) {
                StripePaymentActivity.this.showError(string);
            } else {
                initPaymentSession();
            }
        }, this));
    }

    private void initPaymentSession() {
        paymentSession = new PaymentSession(this);
        paymentSession.init(new PaymentSession.PaymentSessionListener() {

            @Override
            public void onCommunicatingStateChanged(boolean isCommunicating) {
                // Show network activity to user
                if (isCommunicating) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMessage) {
                Utils.log("Error: " + errorMessage);
                StripePaymentActivity.this.showError(getString(R.string.customersession_init_failed));
            }

            @Override
            public void onPaymentSessionDataChanged(@NonNull PaymentSessionData data) {
                buyButton.setEnabled(true);
                selectMethodButton.setEnabled(true);
            }

        }, new PaymentSessionConfig.Builder()
                .setShippingMethodsRequired(false)
                .setShippingInfoRequired(false)
                .build());
    }

    private String buildCardString(@NonNull SourceCardData data) {
        return getString(R.string.credit_card_format_string, data.getLast4());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (paymentSession != null) {
            paymentSession.onDestroy();
        }

        try {
            TUMCabeClient
                    .getInstance(this)
                    .cancelTicketReservation(this, ticketHistory,
                            new Callback<TicketSuccessResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<TicketSuccessResponse> call,
                                                       @NonNull Response<TicketSuccessResponse> response) {
                                    Utils.log("Cancellation Success!");
                                }

                                @Override
                                public void onFailure(@NonNull Call<TicketSuccessResponse> call,
                                                      @NonNull Throwable t) {
                                    Utils.log("Cancellation Error!");
                                }
                            });
        } catch (IOException e) {
            Utils.log(e);
        }
    }

}
