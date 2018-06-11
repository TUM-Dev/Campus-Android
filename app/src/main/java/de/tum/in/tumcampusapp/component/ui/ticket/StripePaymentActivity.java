package de.tum.in.tumcampusapp.component.ui.ticket;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;

import com.stripe.android.view.CardInputWidget;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.overview.MainActivity;

public class StripePaymentActivity extends BaseActivity {

    ProgressDialog progressDialog;
    CardInputWidget cardInputWidget;

    public StripePaymentActivity() {
        super(R.layout.activity_payment_stripe);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cardInputWidget = findViewById(R.id.card_input_widget);

        Button buyButton = findViewById(R.id.complete_purchase_button);
        buyButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     if(isCardValid()) {
                         requestTicket();
                     } else {
                         showError(getString(R.string.card_data_invalid));
                     }
                 }
             }
        );
    }

    //TODO: implement Stripe and Backend interaction for purchase here
    private void requestTicket() {
        setLoading();

        // Get Card info

        // Request token from Stripe

        // Request Ticket synchronously

        // Depending on if there was an error, continue
        finishLoadingSuccess();
    }

    private void setLoading() {
        progressDialog = ProgressDialog.show(StripePaymentActivity.this, "", getString(R.string.purchase_progress_message), true);
    }

    private void finishLoadingSuccess() {
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

    private void finishLoadingError(String error) {
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
        return cardInputWidget.getCard() != null;
    }

}
