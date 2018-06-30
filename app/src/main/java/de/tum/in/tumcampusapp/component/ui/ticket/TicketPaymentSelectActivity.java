package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;


public class TicketPaymentSelectActivity extends BaseActivity {

    public TicketPaymentSelectActivity() {
        super(R.layout.activity_payment_selection);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button stripeButton = findViewById(R.id.creditcard_button);
        stripeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StripePaymentActivity.class);
                startActivity(intent);
            }
        });
    }

}


