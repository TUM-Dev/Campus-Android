package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.TicketEphemeralKeyProvider;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StripePaymentActivity extends BaseActivity {

    private FrameLayout loadingLayout;
    private EditText cardholderEditText;
    private ViewSwitcher selectMethodSwitcher;
    private AppCompatButton purchaseButton;

    private PaymentSession paymentSession;
    private boolean didSelectPaymentMethod;

    private int ticketHistory; // Ticket ID, since the ticket was reserved in the prior activity and
                               // we need the ID to init the purchase

    private String ticketPrice;

    public StripePaymentActivity() {
        super(R.layout.activity_payment_stripe);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ticketPrice = getIntent().getStringExtra(Const.KEY_TICKET_PRICE);
        ticketHistory = getIntent().getIntExtra(Const.KEY_TICKET_HISTORY, -1);

        if (ticketHistory < 0 || ticketPrice == null) {
            Utils.showToast(this, R.string.error_something_wrong);
            finish();
            return;
        }

        initViews();
        initStripeSession();
    }

    private void initViews() {
        loadingLayout = findViewById(R.id.loading_layout);

        String cardholder = Utils.getSetting(this, Const.KEY_CARD_HOLDER, "");
        cardholderEditText = findViewById(R.id.cardholder_edit_text);
        cardholderEditText.setText(cardholder);
        cardholderEditText.setSelection(cardholder.length());

        if (cardholder.isEmpty()) {
            // We only request focus if the user has not entered their name. Otherwise, we assume
            // that the user will perform the payment method selection next.
            cardholderEditText.requestFocus();
        }

        cardholderEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                Utils.setSetting(StripePaymentActivity.this, Const.KEY_CARD_HOLDER, input);
                updateBuyButton();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        selectMethodSwitcher = findViewById(R.id.select_payment_method_switcher);
        selectMethodSwitcher.setOnClickListener(v -> paymentSession.presentPaymentMethodSelection());

        String purchaseButtonString = getString(R.string.buy_format_string, ticketPrice);
        purchaseButton = findViewById(R.id.complete_purchase_button);
        purchaseButton.setText(purchaseButtonString);
        purchaseButton.setOnClickListener(v -> purchaseTicket());
    }

    private void updateBuyButton() {
        boolean hasCardholder = !cardholderEditText.getText().toString().isEmpty();
        boolean enabled = hasCardholder && didSelectPaymentMethod;
        float alpha = enabled ? 1.0f : 0.5f;

        purchaseButton.setEnabled(enabled);
        purchaseButton.setAlpha(alpha);
    }

    private void purchaseTicket() {
        String cardholder = cardholderEditText.getText().toString();
        showLoading(true);

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
                                    Ticket ticket = response.body();
                                    if (ticket != null) {
                                        handleTicketPurchaseSuccess(ticket);
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<Ticket> call, @NonNull Throwable t) {
                                    Utils.log(t);
                                    handleTicketPurchaseFailure();
                                }
                            });
        } catch (NoPrivateKey e) {
            Utils.log(e);
            handleTicketPurchaseFailure();
        }
    }

    private void handleTicketPurchaseSuccess(@NonNull Ticket ticket) {
        showLoading(false);

        List<Ticket> tickets = new ArrayList<>();
        tickets.add(ticket);

        EventsController controller = new EventsController(this);
        controller.insert(tickets.toArray(new Ticket[0]));

        openPaymentConfirmation(ticket);
    }

    private void openPaymentConfirmation(Ticket ticket) {
        Intent intent = new Intent(this, PaymentConfirmationActivity.class);
        intent.putExtra(Const.KEY_EVENT_ID, ticket.getEventId());
        startActivity(intent);
    }

    private void handleTicketPurchaseFailure() {
        showLoading(false);
        showError(getString(R.string.error_something_wrong));
    }

    private void showLoading(boolean isLoading) {
        loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        TransitionManager.beginDelayedTransition(loadingLayout);
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            // Data might be null if user tapped the back button from credit card selection
            return;
        }

        String selectedSource = data.getStringExtra(PaymentMethodsActivity.EXTRA_SELECTED_PAYMENT);
        Source source = Source.fromString(selectedSource);

        // Note: It isn't possible for a null or non-card source to be returned.
        if (source != null && Source.CARD.equals(source.getType())) {
            SourceCardData cardData = (SourceCardData) source.getSourceTypeModel();

            TextView methodTextView = findViewById(R.id.selected_payment_method_text_view);
            TextView cardBrandTextView = findViewById(R.id.selected_payment_method_brand_text_view);

            methodTextView.setText(buildCardString(cardData));
            cardBrandTextView.setText(cardData.getBrand());

            selectMethodSwitcher.showNext();

            didSelectPaymentMethod = true;
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requestAutofillIfEmptyCardholder();
                }

                loadingLayout.setVisibility(View.GONE);
                TransitionManager.beginDelayedTransition(loadingLayout);
            }
        }, this));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void requestAutofillIfEmptyCardholder() {
        if (cardholderEditText.getText().toString().isEmpty()) {
            cardholderEditText.setAutofillHints(View.AUTOFILL_HINT_NAME);

            AutofillManager autofillManager = getSystemService(AutofillManager.class);
            if (autofillManager != null && autofillManager.isEnabled()) {
                autofillManager.requestAutofill(cardholderEditText);
            }
        }
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
                loadingLayout.setVisibility(isCommunicating ? View.VISIBLE : View.GONE);
                TransitionManager.beginDelayedTransition(loadingLayout);
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMessage) {
                Utils.log("Error: " + errorMessage);
                showError(getString(R.string.customersession_init_failed));
            }

            @Override
            public void onPaymentSessionDataChanged(@NonNull PaymentSessionData data) {
                purchaseButton.setEnabled(true);
                selectMethodSwitcher.setEnabled(true);
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
    }

}
