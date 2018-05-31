package de.tum.in.tumcampusapp.component.ui.ticket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.common.collect.ImmutableList;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.List;

import de.tum.in.tumcampusapp.utils.Utils;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class TicketScanActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        List<BarcodeFormat> formats = ImmutableList.of(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);

        // Set the scanner inside the frame layout view as the content view
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        mScannerView.stopCamera();           // Stop camera on pause
        super.onPause();
    }

    @Override
    public void handleResult(Result rawResult) {

        Utils.log(rawResult.getText()); // Prints scan results
        Utils.log(rawResult.getBarcodeFormat()
                .toString()); // Prints the scan format (qrcode, pdf417 etc.)
        Intent data = new Intent();
        data.putExtra("name", rawResult.getText());

        // TODO: Send the String to the Server and ask the server to return the Name associated with it
        // Until then, we will display a dummy string

        // 3. If correct, user can press "Confirm", if not, user can "Abort" and scan again
        setResult(RESULT_OK, data);
        finish();
    }

}
