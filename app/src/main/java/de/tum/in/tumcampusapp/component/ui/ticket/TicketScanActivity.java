package de.tum.in.tumcampusapp.component.ui.ticket;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.common.collect.ImmutableList;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class TicketScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_ticket_scan);

        mScannerView = findViewById(R.id.scanner_view);

        List<BarcodeFormat> formats = ImmutableList.of(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);
    }

    @Override
    protected void onStart() {
        super.onStart();
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.CAMERA)
                .subscribe(granted -> {
                    if (!granted) { //
                        finish();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        String code = rawResult.getText();
        ConfirmCheckInFragment fragment = ConfirmCheckInFragment.newInstance("1", code);
        fragment.show(getSupportFragmentManager(), "confirm_check_in_fragment");

        // TODO: Send the String to the Server and ask the server to return the Name associated with it
        // Until then, we will display a dummy string

    }

}
