package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.material.button.MaterialButton;
import com.stripe.android.CustomerSession;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.model.Source;
import com.stripe.android.model.SourceCardData;
import com.stripe.android.view.PaymentMethodsActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.TicketEphemeralKeyProvider;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StripePaymentActivity extends BaseActivity {

    private FrameLayout loadingLayout;
    private EditText cardholderEditText;
    private ViewSwitcher selectMethodSwitcher;
    private MaterialButton purchaseButton;
    private CheckBox termsOfServiceCheckBox;

    private PaymentSession paymentSession;
    private boolean didSelectPaymentMethod;

    private List<Integer> ticketIds; // Ticket ID, since the ticket was reserved in the prior activity and
                               // we need the ID to init the purchase

    private String ticketPrice;
    private String termsOfServiceLink;
    private String stripePublishableKey;

    private TicketsLocalRepository localTicketRepo;

    public StripePaymentActivity() {
        super(R.layout.activity_payment_stripe);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localTicketRepo = new TicketsLocalRepository(TcaDb.getInstance(this));

        ticketPrice = getIntent().getStringExtra(Const.KEY_TICKET_PRICE);
        ticketIds = getIntent().getIntegerArrayListExtra(Const.KEY_TICKET_IDS);
        termsOfServiceLink = getIntent().getStringExtra(Const.KEY_TERMS_LINK);
        stripePublishableKey = getIntent().getStringExtra(Const.KEY_STRIPE_API_PUBLISHABLE_KEY);

        if (ticketIds.isEmpty()
            || ticketPrice == null
            || termsOfServiceLink.isEmpty()
            || stripePublishableKey == null) {
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
        selectMethodSwitcher.setOnClickListener(v -> {
            if (paymentSession != null) {
                paymentSession.presentPaymentMethodSelection();
            }
        });

        String purchaseButtonString = getString(R.string.buy_format_string, ticketPrice);
        purchaseButton = findViewById(R.id.complete_purchase_button);
        purchaseButton.setText(purchaseButtonString);
        purchaseButton.setOnClickListener(v -> purchaseTicket());

        termsOfServiceCheckBox = findViewById(R.id.terms_of_service_checkbox);
        termsOfServiceCheckBox.setOnClickListener((view) -> updateBuyButton());

        findViewById(R.id.terms_of_service_button).setOnClickListener((view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(termsOfServiceLink));
            view.getContext().startActivity(browserIntent);
        }));
    }

    private void updateBuyButton() {
        boolean hasCardholder = !cardholderEditText.getText().toString().isEmpty();
        boolean enabled = hasCardholder
                && didSelectPaymentMethod
                && termsOfServiceCheckBox.isChecked();
        float alpha = enabled ? 1.0f : 0.5f;

        purchaseButton.setEnabled(enabled);
        purchaseButton.setAlpha(alpha);
    }

    private void purchaseTicket() {
        String cardholder = cardholderEditText.getText().toString();
        showLoading(true);

        try {
            String methodId = paymentSession.getPaymentSessionData().getSelectedPaymentMethodId();
            if (methodId == null) {
                Utils.showToast(this, R.string.error_something_wrong);
                return;
            }

            TUMCabeClient
                    .getInstance(this)
                    .purchaseTicketStripe(this, ticketIds,
                                          methodId, cardholder, new Callback<List<Ticket>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Ticket>> call,
                                               @NonNull Response<List<Ticket>> response) {
                            List<Ticket> tickets = response.body();
                            if (!tickets.isEmpty()) {
                                handleTicketPurchaseSuccess(tickets);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<Ticket>> call, @NonNull Throwable t) {
                            Utils.log(t);
                            handleTicketPurchaseFailure();
                        }
                    });
        } catch (NoPrivateKey e) {
            Utils.log(e);
            handleTicketPurchaseFailure();
        }
    }

    private void handleTicketPurchaseSuccess(@NonNull List<Ticket> tickets) {
        showLoading(false);
        localTicketRepo.insert(tickets.toArray(new Ticket[0]));
        openPaymentConfirmation(tickets);
    }

    private void openPaymentConfirmation(List<Ticket> tickets) {
        Intent intent = new Intent(this, PaymentConfirmationActivity.class);
        intent.putExtra(Const.KEY_EVENT_ID, tickets.get(0).getEventId());
        intent.putExtra(Const.KEY_TICKET_AMOUNT, tickets.size());
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
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
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
        PaymentConfiguration.init(stripePublishableKey);
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
                showLoading(false);
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
                updateBuyButton();
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
