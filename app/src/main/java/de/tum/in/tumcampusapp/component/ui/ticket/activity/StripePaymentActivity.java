package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.CustomerSession;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.model.Source;
import com.stripe.android.model.SourceCardData;
import com.stripe.android.view.PaymentMethodsActivity;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.overview.MainActivity;
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

    private ProgressDialog progressDialog;
    private Button savedCardsButton;
    private AppCompatButton buyButton;
    private ProgressBar progressBar;
    private EditText cardholderEditText;

    private PaymentSession paymentSession;
    private Boolean setSource = false;
    private int ticketHistory;
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
        buyButton = findViewById(R.id.complete_purchase_button);
        buyButton.setOnClickListener(v -> requestTicket());
        buyButton.setEnabled(false); // disabled until customer- and payment session have been loaded

        cardholderEditText = findViewById(R.id.cardholder_edit_text);

        progressBar = findViewById(R.id.stripe_purchase_progress);
        progressBar.setVisibility(View.INVISIBLE);

        savedCardsButton = findViewById(R.id.saved_cards_button);
        savedCardsButton.setOnClickListener(view -> paymentSession.presentPaymentMethodSelection());
        savedCardsButton.setEnabled(false);

        // Insert formatted string to remind users about which amount they are going to pay
        TextView priceReminder = findViewById(R.id.payment_info_price_textview);
        priceReminder.setText(getString(R.string.payment_info_price_reminder, price));
    }

    private void requestTicket() {
        String cardholder = cardholderEditText.getText().toString();
        if (cardholder.isEmpty()) {
            Utils.showToast(getApplicationContext(), R.string.empty_cardholder_message);
            return;
        }

        if (!setSource) {
            // No payment source selected yet
            Utils.showToast(getApplicationContext(), R.string.card_data_invalid);
            return;
        }

        setPurchaseRequestLoading();
        try {
            TUMCabeClient
                    .getInstance(this)
                    .purchaseTicketStripe(this, ticketHistory,
                            paymentSession.getPaymentSessionData().getSelectedPaymentMethodId(),
                            cardholder, new Callback<Ticket>() {
                                @Override
                                public void onResponse(@NonNull Call<Ticket> call, @NonNull Response<Ticket> response) {
                                    EventsController ec = new EventsController(StripePaymentActivity.this);
                                    List<Ticket> ticketList = new ArrayList<>();
                                    ticketList.add(response.body());
                                    ec.addTickets(ticketList);
                                    finishLoadingPurchaseRequestSuccess(response.body());
                                }

                                @Override
                                public void onFailure(@NonNull Call<Ticket> call, @NonNull Throwable t) {
                                    Utils.log(t);
                                    finishLoadingPurchaseRequestError(getString(R.string.ticket_retrieval_error));
                                    finish();
                                }
                            });
        } catch (IOException exception) {
            Utils.log(exception);
            finishLoadingPurchaseRequestError(getString(R.string.purchase_error_message));
        }
    }

    private void setPurchaseRequestLoading() {
        progressDialog = ProgressDialog.show(this, null,
                getString(R.string.purchase_progress_message), true);
    }

    private void finishLoadingPurchaseRequestSuccess(Ticket ticket) {
        progressDialog.dismiss();

        // Show success message to user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.purchase_success_title))
                .setMessage(getString(R.string.purchase_success_message))
                .setPositiveButton(getString(R.string.purchase_success_continue), (dialogInterface, i) -> {
                    Intent intent = new Intent(this, ShowTicketActivity.class);
                    intent.putExtra("eventID", ticket.getEventId());
                    startActivity(intent);
                })
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void finishLoadingPurchaseRequestError(String error) {
        progressDialog.dismiss();
        showError(error);
    }

    private void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error))
                .setMessage(message);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
                savedCardsButton.setText(buildCardString(cardData));
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
                savedCardsButton.setEnabled(true);
            }

        }, new PaymentSessionConfig.Builder()
                .setShippingMethodsRequired(false)
                .setShippingInfoRequired(false)
                .build());
    }

    private String buildCardString(@NonNull SourceCardData data) {
        return data.getBrand() + ",  " + getString(R.string.creditcard_ending_in) + "  " + data.getLast4();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (paymentSession != null) {
            paymentSession.onDestroy();
        }
        try {
            TUMCabeClient.getInstance(getApplicationContext())
                    .cancelTicketReservation(StripePaymentActivity.this,
                            ticketHistory, new Callback<TicketSuccessResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<TicketSuccessResponse> call,
                                                       @NonNull Response<TicketSuccessResponse> response) {
                                    System.out.println("Cancalation Success!");
                                }

                                @Override
                                public void onFailure(@NonNull Call<TicketSuccessResponse> call, @NonNull Throwable t) {
                                    System.err.println("Cancalation Error!");
                                }
                            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
