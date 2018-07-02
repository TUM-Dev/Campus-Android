package de.tum.in.tumcampusapp.component.ui.ticket;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservationResponse;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

public class StripePaymentActivity extends BaseActivity {

    ProgressDialog progressDialog;
    Button savedCardsButton;
    AppCompatButton buyButton;
    ProgressBar progressBar;
    EditText cardholderEditText;

    PaymentSession paymentSession;
    Boolean setSource = false;
    int ticketHistory;
    int eventID;
    double price = -1; // ticket price IN CENTS


    public StripePaymentActivity() {
        super(R.layout.activity_payment_stripe);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get price from intent; convert it to CENTS (as required by Stripe)
        price = getIntent().getDoubleExtra("ticketPrice", -1.0);
        // Reserve ticket
        ticketHistory = reserveTicket();

        if (ticketHistory < 0 || price < 0) {
            Toast.makeText(getApplicationContext(), R.string.internal_error, Toast.LENGTH_LONG).show();
            finish();
        }

        initSubviews();
        initStripeSession();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        paymentSession.onDestroy();
        try {
            TUMCabeClient.getInstance(getApplicationContext()).cancelTicketReservation(ticketHistory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initSubviews() {
        buyButton = findViewById(R.id.complete_purchase_button);
        buyButton.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             requestTicket();
                                         }
                                     }
        );

        cardholderEditText = findViewById(R.id.cardholder_edit_text);

        progressBar = findViewById(R.id.stripe_purchase_progress);
        progressBar.setVisibility(View.INVISIBLE);

        savedCardsButton = findViewById(R.id.saved_cards_button);
        savedCardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentSession.presentPaymentMethodSelection();
            }
        });

        // Create formated string to remind users about which amount they are going to pay
        TextView priceReminder = findViewById(R.id.payment_info_price_textview);
        MessageFormat fmt = new MessageFormat(getString(R.string.payment_info_price_reminder));
        Object[] args = {String.format("%.2f", getIntent().getDoubleExtra("ticketPrice", -1.0))};
        priceReminder.setText(fmt.format(args));
    }


    private void requestTicket() {
        String cardholder = cardholderEditText.getText().toString();
        if (cardholder.length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.empty_cardholder_message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!setSource) {
            // No payment source selected yet
            Toast.makeText(getApplicationContext(), R.string.card_data_invalid, Toast.LENGTH_LONG).show();
            return;
        }

        setPurchaseRequestLoading();
        try {
            Ticket ticket = TUMCabeClient
                    .getInstance(StripePaymentActivity.this)
                    .purchaseTicketStripe(1,
                            paymentSession.getPaymentSessionData().getSelectedPaymentMethodId(),
                            getUserMailAddress(),
                            cardholder);
            //TODO: Add Ticket to local database and jump to ShowTicketActivity
            EventsController ec = new EventsController(StripePaymentActivity.this);
            List<Ticket> ticketList = new ArrayList<>();
            ticketList.add(ticket);
            ec.addTickets(ticketList);
            finishLoadingPurchaseRequestSuccess(ticket);
        } catch (IOException exception) {
            exception.printStackTrace();
            finishLoadingPurchaseRequestError(getString(R.string.purchase_error_message));
            return;
        }
    }


    private void setPurchaseRequestLoading() {
        progressDialog = ProgressDialog.show(StripePaymentActivity.this, "", getString(R.string.purchase_progress_message), true);
    }


    private void finishLoadingPurchaseRequestSuccess(Ticket ticket) {
        progressDialog.dismiss();

        // Show success message to user
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
        builder.setTitle(getString(R.string.purchase_success_title))
                .setMessage(getString(R.string.purchase_success_message))
                .setPositiveButton(getString(R.string.purchase_success_continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(StripePaymentActivity.this, MainActivity.class);
                        startActivity(intent);
                        intent = new Intent(StripePaymentActivity.this, ShowTicketActivity.class);
                        intent.putExtra("eventID", ticket.getEventId());
                        startActivity(intent);
                    }
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
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
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
        initPaymentSession();
    }


    private void initCustomerSession() {
        String customerMail = getUserMailAddress();

        CustomerSession.initCustomerSession(new TicketEphemeralKeyProvider(new TicketEphemeralKeyProvider.ProgressListener() {
            @Override
            public void onStringResponse(String string) {
                if (string.startsWith("Error: ")) {
                    showError(string);
                }
            }
        }, getApplicationContext(), customerMail));
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
                System.out.println("Error: " + errorMessage);
                showError(getString(R.string.customersession_init_failed));
            }

            @Override
            public void onPaymentSessionDataChanged(@NonNull PaymentSessionData data) {
            }

        }, new PaymentSessionConfig.Builder()
                .setShippingMethodsRequired(false)
                .setShippingInfoRequired(false)
                .build());
    }

    private String buildCardString(@NonNull SourceCardData data) {
        return data.getBrand() + ",  " + getString(R.string.creditcard_ending_in) + "  " + data.getLast4();
    }

    private int reserveTicket() {
        int ticketType = getIntent().getIntExtra("ticketType", -1);
        if (ticketType <= 0) {
            Toast.makeText(getApplicationContext(), R.string.internal_error, Toast.LENGTH_LONG).show();
            finish();
        }

        try {
            TicketReservationResponse response = TUMCabeClient.getInstance(getApplicationContext()).reserveTicket(StripePaymentActivity.this, ticketType);
            return response.getTicketHistory();
        } catch (IOException exception) {
            showError(getString(R.string.internal_error));
            return -1;
        }
    }

    private String getUserMailAddress() {
        String customerMail = Utils.getSetting(StripePaymentActivity.this, Const.LRZ_ID, "");
        if(customerMail.length() == 0) {
            showError(getString(R.string.internal_error));
            finish();
        }
        return customerMail + "@mytum.de";
    }

}
