package de.tum.in.tumcampusapp.component.ui.tufilm;

import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import de.tum.in.tumcampusapp.R;

public class show_ticket extends AppCompatActivity {

    private TextView moviedetailTextView;
    private TextView ticketsnumberTextView;
    private ImageView ticketqrcode;
    /*
        public TuitionFeesActivity() {
                super(TUMOnlineConst.Companion.getTUITION_FEE_STATUS(), R.layout.activity_tuitionfees);
            }
    */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        moviedetailTextView = findViewById(R.id.moviedetail);
        ticketsnumberTextView = findViewById(R.id.ticketnumber);
        ticketqrcode = findViewById(R.id.ticket_qrcode);
        moviedetailTextView.setText(" KingsMan 08.05 16:00-17:00\n" +
                "1. Stock, Hörsaal 1200 (Carl-von-Linde-Hörsaal)\n" +
                "Arcisstraße 21");//data from last activity
        ticketsnumberTextView.setText("87237489273984");//data from backend
        //ticketqrcode.setImageResource();
        String text="KingsMan 08.05 Kailiang Dong 03695880"; // Whatever you need to encode in the QR code
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ticketqrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }
}


