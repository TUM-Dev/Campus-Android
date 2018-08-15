package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

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

    private ViewSwitcher selectMethodSwitcher;

    //private TextView selectMethodButton;
    //private LinearLayout selectedMethodLayout;
    //private TextView selectedMethodTextView;
    //private TextView selectedMethodCardTypeTextView;

    private ViewSwitcher completePurchaseSwitcher;

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
            Utils.showToast(this, R.string.internal_error);
            finish();
            return;
        }

        initSubviews();
        initStripeSession();
    }

    private void initSubviews() {
        cardholderEditText = findViewById(R.id.cardholder_edit_text);
        cardholderEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateBuyButton();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        selectMethodSwitcher = findViewById(R.id.select_payment_method_switcher);
        selectMethodSwitcher.setOnClickListener(v -> paymentSession.presentPaymentMethodSelection());

        // TODO: Init later
        /*
        selectedMethodLayout = findViewById(R.id.selected_payment_method_container);
        selectedMethodTextView = findViewById(R.id.selected_payment_method_text_view);
        selectedMethodCardTypeTextView = findViewById(R.id.selected_payment_method_type_text_view);
        */

        completePurchaseSwitcher = findViewById(R.id.complete_purchase_switcher);

        String buyButtonString = getString(R.string.buy_format_string, price);
        buyButton = findViewById(R.id.complete_purchase_button);
        buyButton.setText(buyButtonString);
        buyButton.setOnClickListener(v -> requestTicket());
    }

    private void updateBuyButton() {
        boolean hasCardholder = !cardholderEditText.getText().toString().isEmpty();
        boolean enabled = hasCardholder && setSource;
        float alpha = enabled ? 1.0f : 0.5f;
        buyButton.setEnabled(enabled);
        buyButton.setAlpha(alpha);
    }

    private void requestTicket() {
        String cardholder = cardholderEditText.getText().toString();
        setPurchaseRequestLoading();

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

    private void setPurchaseRequestLoading() {
        completePurchaseSwitcher.showNext();
        //progressBar.setVisibility(showLoading ? View.VISIBLE : View.GONE);
        //buyButton.setVisibility(showLoading ? View.GONE : View.VISIBLE);
    }

    private void finishLoadingPurchaseRequestSuccess(Ticket ticket) {
        setPurchaseRequestLoading();

        Intent intent = new Intent(this, PaymentConfirmationActivity.class);
        intent.putExtra("eventID", ticket.getEventId());
        startActivity(intent);
    }

    private void finishLoadingPurchaseRequestError(String error) {
        setPurchaseRequestLoading();
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
        if (data == null) {
            // Data might be null if user tapped Back button from Credit Card selection
            return;
        }

        String selectedSource = data.getStringExtra(PaymentMethodsActivity.EXTRA_SELECTED_PAYMENT);
        Source source = Source.fromString(selectedSource);

        // Note: it isn't possible for a null or non-card source to be returned.
        if (source != null && Source.CARD.equals(source.getType())) {
            SourceCardData cardData = (SourceCardData) source.getSourceTypeModel();

            //LinearLayout selectedMethodLayout = findViewById(R.id.selected_payment_method_container);
            TextView methodTextView = findViewById(R.id.selected_payment_method_text_view);
            TextView cardBrandTextView = findViewById(R.id.selected_payment_method_type_text_view);

            methodTextView.setText(buildCardString(cardData));
            cardBrandTextView.setText(cardData.getBrand());

            selectMethodSwitcher.showNext();

            setSource = true;
            updateBuyButton();
        }
    }

    private void initStripeSession() {
        PaymentConfiguration.init(Const.STRIPE_API_PUBLISHABLE_KEY);
        initCustomerSession();
    }

    private void initCustomerSession() {
        CustomerSession.initCustomerSession(new TicketEphemeralKeyProvider(string -> {
            if (string.startsWith("Error: ")) {
                showError(string);
                finish();
            } else {
                initPaymentSession();
            }
        }, this));
    }

    private void initPaymentSession() {
        PaymentSessionConfig config = new PaymentSessionConfig.Builder()
                .setShippingMethodsRequired(false)
                .setShippingInfoRequired(false)
                .build();

        paymentSession = new PaymentSession(this);
        paymentSession.init(new PaymentSession.PaymentSessionListener() {

            @Override
            public void onCommunicatingStateChanged(boolean isCommunicating) {
                // Show network activity to user
                // TODO
                /*
                if (isCommunicating) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
                */
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMessage) {
                Utils.log("Error: " + errorMessage);
                showError(getString(R.string.customersession_init_failed));
            }

            @Override
            public void onPaymentSessionDataChanged(@NonNull PaymentSessionData data) {
                buyButton.setEnabled(true);
                // TODO: updateBuyButton();
                selectMethodSwitcher.setEnabled(true);
                //selectMethodButton.setEnabled(true);
            }

        }, config);
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

        // TODO

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
