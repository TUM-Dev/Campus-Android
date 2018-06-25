package de.tum.in.tumcampusapp.component.ui.ticket;

import android.graphics.Bitmap;
import android.os.Bundle;
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
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;

public class ShowTicketActivity extends BaseActivity {

    private TextView eventDetailsTextView;
    private ImageView ticketQrCode;

    private EventsController eventsController;

    public ShowTicketActivity() {
        super(R.layout.activity_show_ticket);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventsController = new EventsController(this);

        eventDetailsTextView = findViewById(R.id.eventdetail);
        ticketQrCode = findViewById(R.id.ticket_qrcode);

        int eventId = getIntent().getIntExtra("eventID", 0);

        Ticket ticket = eventsController.getTicketByEventId(eventId);
        Event event = eventsController.getEventById(ticket.getEventId());

        String dateString = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.GERMANY).
                format(event.getDate());
        //load eventdetail
        String eventdetail = event.getTitle() +
                "\n" + event.getLocality() +
                "\n" + dateString;
        eventDetailsTextView.setText(eventdetail);

        //create the qrcode using library  zxing
        createQRCode(ticket.getCode());

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


