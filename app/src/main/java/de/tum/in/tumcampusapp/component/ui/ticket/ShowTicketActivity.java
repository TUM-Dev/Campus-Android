package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Intent;
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

public class ShowTicketActivity extends BaseActivity {

    private TextView movieDetailsTextView;
    private TextView ticketNumberTextView;
    private ImageView ticketQrCode;

    public ShowTicketActivity() {
        super(R.layout.activity_show_ticket);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieDetailsTextView = (TextView) findViewById(R.id.moviedetail);
        ticketNumberTextView = (TextView) findViewById(R.id.ticketnumber);
        ticketQrCode = (ImageView) findViewById(R.id.ticket_qrcode);
        //Get data from EventsDetailsFragment
        Intent intent = getIntent();
        String data = intent.getStringExtra("movie_data");
        movieDetailsTextView.setText(data);
        //get objectclass

        //TODO:It is data from backend. Wait for setting up of backend
        ticketNumberTextView.setText("87237489273984");

        // TODO:it is Whatever you need to encode in the QR code.Wait for setting up of backend
        String text = "Kailiang Dong";//Here should be a random 128 character string

        //create the qrcode using library  zxing
        createQRCode(text);

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


