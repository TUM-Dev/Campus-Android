package de.tum.in.tumcampusapp.component.ui.ticket;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.stripe.android.CustomerSession;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.model.Customer;
import com.stripe.android.view.CardInputWidget;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.overview.MainActivity;
import de.tum.in.tumcampusapp.utils.Const;

public class StripePaymentActivity extends BaseActivity {

    ProgressDialog progressDialog;
    CardInputWidget cardInputWidget;
    Button savedCardsButton;
    Button buyButton;
    ProgressBar stripeProgressBar;

    PaymentSession paymentSession;


    public StripePaymentActivity() {
        super(R.layout.activity_payment_stripe);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: remove, only for testing
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        cardInputWidget = findViewById(R.id.card_input_widget);

        initStripeSession();
        initSubviews();
    }


    private void initSubviews() {
        buyButton = findViewById(R.id.complete_purchase_button);
        buyButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (isCardValid()) {
                     requestTicket();
                 } else {
                     showError(getString(R.string.card_data_invalid));
                 }
             }
            }
        );

        savedCardsButton = findViewById(R.id.saved_cards_button);
        savedCardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentSession.presentPaymentMethodSelection();
            }
        });
        savedCardsButton.setEnabled(false);

        stripeProgressBar = findViewById(R.id.stripe_session_init_progressbar);
        stripeProgressBar.animate();
    }


    //TODO: implement Stripe and Backend interaction for purchase here
    private void requestTicket() {
        setPurchaseRequestLoading();

        // Get Card info

        // Request token from Stripe

        // Request Ticket synchronously

        // Depending on if there was an error, continue
        finishLoadingPurchaseRequestSuccess();
    }

    private void setPurchaseRequestLoading() {
        progressDialog = ProgressDialog.show(StripePaymentActivity.this, "", getString(R.string.purchase_progress_message), true);
    }

    private void finishLoadingPurchaseRequestSuccess() {
        progressDialog.dismiss();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        // Show success message to user
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
        builder.setTitle(getString(R.string.purchase_success_title))
                .setMessage(getString(R.string.purchase_success_message))
                .setPositiveButton(getString(R.string.purchase_success_continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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


    private boolean isCardValid() {
        /* Stripe component returns null when client-side checks conclude to invalid card data */
        return cardInputWidget.getCard() != null;
    }


    /* On return of the saved creditcard selection */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*String selectedSource = data.getStringExtra(PaymentMethodsActivity.EXTRA_SELECTED_PAYMENT);
        Source source = Source.fromString(selectedSource);
        // Note: it isn't possible for a null or non-card source to be returned.
        if (source != null && Source.CARD.equals(source.getType())) {
            SourceCardData cardData = (SourceCardData) source.getSourceTypeModel();
        }*/
    }

    /* Set status of button and progressbar for the credit card overview */
    private void setInitStateLoading(boolean val) {
        if(val) {
            stripeProgressBar.setVisibility(View.VISIBLE);
            savedCardsButton.setEnabled(false);
            savedCardsButton.setBackgroundColor(getResources().getColor(R.color.text_dark_gray));
        } else {
            stripeProgressBar.setVisibility(View.INVISIBLE);
            savedCardsButton.setEnabled(true);
            savedCardsButton.setBackgroundColor(getResources().getColor(R.color.tum_200));
        }
    }


    private void initStripeSession() {
        setInitStateLoading(true);
        PaymentConfiguration.init(Const.STRIPE_API_PUBLISHABLE_KEY);
        initCustomerSession();
    }


    private void initCustomerSession() {
        CustomerSession.initCustomerSession(new TicketEphemeralKeyProvider(new TicketEphemeralKeyProvider.ProgressListener() {
            @Override
            public void onStringResponse(String string) {
                if (string.startsWith("Error: ")) {
                    // Show the error to the user.
                    System.out.println(string);
                    return;
                }
            }
        }, getApplicationContext()));
        CustomerSession.getInstance().retrieveCurrentCustomer(
                new CustomerSession.CustomerRetrievalListener() {
                    @Override
                    public void onCustomerRetrieved(@NonNull Customer customer) {
                        initPaymentSession();
                    }

                    @Override
                    public void onError(int errorCode, @Nullable String errorMessage) {
                        showError(errorMessage);
                    }
                }
        );
    }


    private void initPaymentSession() {
        paymentSession = new PaymentSession(this);

        boolean initSuccess = paymentSession.init(new PaymentSession.PaymentSessionListener() {

            @Override
            public void onCommunicatingStateChanged(boolean isCommunicating) {
                if (isCommunicating) {
                    System.out.println("eiwfub");
                } else {
                    System.out.println("eiwfub");
                }
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMessage) {
                showError(errorMessage);
            }

            @Override
            public void onPaymentSessionDataChanged(@NonNull PaymentSessionData data) {
                System.out.println(data.describeContents());
            }
        }, new PaymentSessionConfig.Builder().build());

        if (initSuccess) {
            setInitStateLoading(false);
        }
    }

}
