package de.tum.in.tumcampusapp.component.ui.ticket.activity;

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
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.joda.time.DateTime;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.utils.Utils;

public class ShowTicketActivity extends BaseActivity {

    private TextView eventLocationTextView;
    private ImageView ticketQrCodeImageView;
    private TextView eventTitleTextView;
    private TextView eventDateTimeTextView;
    private TextView eventPriceTextView;
    private TextView eventRedemptionStateTextView;

    private Ticket ticket;
    private Event event;
    private TicketType ticketType;

    private String eventTitle;
    private String eventLocation;
    private String eventDateTime;
    private String eventPrice;
    private String redemptionState;

    public ShowTicketActivity() {
        super(R.layout.activity_show_ticket);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        initializeViews();
        getTicketDataFromController();
        setEventAndTicketDetails();
        setViewData();
        createQRCode(ticket.getCode());
        setWindowBrightnessToFull();
    }

    private void initializeViews() {
        eventLocationTextView = findViewById(R.id.ticket_event_location);
        eventTitleTextView = findViewById(R.id.ticket_event_title);
        eventDateTimeTextView = findViewById(R.id.ticket_event_date_time);
        eventPriceTextView = findViewById(R.id.ticket_event_price);
        eventRedemptionStateTextView = findViewById(R.id.ticket_event_redemption_state);
        ticketQrCodeImageView = findViewById(R.id.ticket_qr_code);
    }

    private void getTicketDataFromController() {
        EventsController eventsController = new EventsController(this);
        int eventId = getIntent().getIntExtra("eventID", 0);

        ticket = eventsController.getTicketByEventId(eventId);
        event = eventsController.getEventById(ticket.getEventId());
        ticketType = eventsController.getTicketTypeById(ticket.getTicketTypeId());
    }

    private void setEventAndTicketDetails() {
        eventTitle = event.getTitle();
        eventLocation = event.getLocality();
        eventDateTime = Event.methods.getFormattedDateTime(this, event.getStart());
        eventPrice = "";
        if (ticketType != null) {
            eventPrice = ticketType.getFormattedPrice();
        }

        // set redemption string
        DateTime redemptionDate = ticket.getRedemption();
        String formattedDateTime = redemptionDate == null ? this.getString(R.string.no) :
                Event.methods.getFormattedDateTime(this, redemptionDate);
        redemptionState = this.getString(R.string.redeemed, formattedDateTime);
    }

    private void setViewData() {
        eventTitleTextView.setText(eventTitle);
        eventDateTimeTextView.setText(eventDateTime);
        eventPriceTextView.setText(eventPrice);
        eventRedemptionStateTextView.setText(redemptionState);

        eventLocationTextView.setText(eventLocation);
        // underline location text to indicate that it is linked to google map
        eventLocationTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        eventLocationTextView.setOnClickListener(view -> showMap());
    }

    private void showMap() {
        eventLocationTextView.setTextColor(Color.RED);
        String map = "http://maps.google.co.in/maps?q=" + eventLocationTextView.getText();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        startActivity(mapIntent);
    }

    private void setWindowBrightnessToFull() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        window.setAttributes(lp);
    }

    private void createQRCode(String text) {
        Writer multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ticketQrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Utils.log(e);
        }
    }
}


