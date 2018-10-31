package de.tum.in.tumcampusapp.component.ui.chat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Collections;
import java.util.List;

import de.tum.in.tumcampusapp.utils.Utils;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class JoinRoomScanActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        List<BarcodeFormat> formats = Collections.singletonList(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);

        // Set the scanner inside the framelayout view as the content view
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
        // Do something with the result here
        Utils.log(rawResult.getText()); // Prints scan results
        Utils.log(rawResult.getBarcodeFormat()
                           .toString()); // Prints the scan format (qrcode, pdf417 etc.)
        Intent data = new Intent();
        data.putExtra("name", rawResult.getText());
        setResult(RESULT_OK, data);
        finish();
    }
}