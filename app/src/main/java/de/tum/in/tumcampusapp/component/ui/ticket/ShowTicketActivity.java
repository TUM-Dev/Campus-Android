package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;


import java.text.SimpleDateFormat;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;

public class ShowTicketActivity extends BaseActivity {
    private TextView eventTitleTextView;
    private TextView eventDetailsTextView;
    private TextView eventLocationTextView;
    private ImageView ticketQrCode;

    public ShowTicketActivity() {
        super(R.layout.activity_show_ticket);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        eventTitleTextView = (TextView) findViewById(R.id.ticket_event_title);
        eventLocationTextView = (TextView) findViewById(R.id.ticket_event_location);
        eventDetailsTextView = (TextView) findViewById(R.id.eventdetail);
        ticketQrCode = (ImageView) findViewById(R.id.ticket_qrcode);

        int eventId = getIntent().getIntExtra("eventID", 0);

        //Get data from Api backend, now it is mock up data
        Ticket ticket = TicketsController.getTicketByEventId(eventId);
        String timeString = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.GERMANY).
                format(ticket.getEvent().getDate());
        String[] time = timeString.split(" ");
        //set date format to current locale setting
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        String datestring = dateFormat.format(ticket.getEvent().getDate());
        //load eventdetail
        String eventtitle = ticket.getEvent().getTitle();
        String eventlocation = ticket.getEvent().getLocality();
        String eventdetail = datestring + " " + time[1] +
                "\n" + "Price: " + ticket.getType().getPrice();
        eventTitleTextView.setText(eventtitle);

        eventDetailsTextView.setText(eventdetail);

        //set locationtext underline to show it can link to google map
        eventLocationTextView.setText(eventlocation);
        eventLocationTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        eventLocationTextView.setOnClickListener(view -> showMap());
        String code = ticket.getCode();
        //create the qrcode using library  zxing
        createQRCode(code);
        //set current screen brightness 100%
        setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);


    }

    private void showMap() {
        eventLocationTextView.setTextColor(Color.RED);
        String map = "http://maps.google.co.in/maps?q=" + eventLocationTextView.getText();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        startActivity(mapIntent);
    }

    private void setWindowBrightness(float brightness) {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness;
        window.setAttributes(lp);
    }

    private void createQRCode(String text) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ticketQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}


