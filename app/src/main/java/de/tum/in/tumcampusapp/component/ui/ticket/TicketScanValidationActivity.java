package de.tum.in.tumcampusapp.component.ui.ticket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;

public class TicketScanValidationActivity extends BaseActivity {

    private String nameOfBuyer;

    public TicketScanValidationActivity() {
        super(R.layout.ticket_buyer_name_display);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_scan_validation);
        //TODO when button "Abort" is clicked: go back to camera view, when button "Confirm" clicked, save the person as a checked in guest
    }
}

