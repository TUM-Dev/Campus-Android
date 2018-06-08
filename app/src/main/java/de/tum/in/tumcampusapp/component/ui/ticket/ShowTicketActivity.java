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

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;

public class ShowTicketActivity extends BaseActivity {

    private TextView eventDetailsTextView;
    private ImageView ticketQrCode;

    public ShowTicketActivity() {
        super(R.layout.activity_show_ticket);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventDetailsTextView = (TextView) findViewById(R.id.eventdetail);
        ticketQrCode = (ImageView) findViewById(R.id.ticket_qrcode);

        //Get data from Api backend, now it is mock up data
        Ticket ticket = TicketsController.getTickets();

        //load eventdetail
        String eventdetail = ticket.getEvent().getTitle() + "\n" + ticket.getEvent().getLocality() + "\n" + ticket.getEvent().getDate();
        eventDetailsTextView.setText(eventdetail);

        String code = ticket.getCode();
        //create the qrcode using library  zxing
        createQRCode(code);

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


