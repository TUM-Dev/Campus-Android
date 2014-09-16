package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.security.KeyChain;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.EduroamManager;

public class SetupEduroam extends ActionBarActivity {

    private EditText lrz;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
        setContentView(R.layout.activity_setup_eduroam);

        // Enable 'More Info' links
        ((TextView)findViewById(R.id.text_with_link_1)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView)findViewById(R.id.text_with_link_2)).setMovementMethod(LinkMovementMethod.getInstance());

        if(Build.VERSION.SDK_INT>=18) {
            findViewById(R.id.certificate).setVisibility(View.GONE);
        }

        lrz = (EditText) findViewById(R.id.wifi_lrz_id);
        lrz.setText(Utils.getSetting(this, Const.LRZ_ID));
        password = (EditText) findViewById(R.id.wifi_password);
        password.requestFocus();
    }

    public void onClickSetup(View v) {
        EduroamManager manager = new EduroamManager(getApplicationContext());
        boolean success = manager.configureEduroam(lrz.getText().toString(), password.getText().toString());
        if(success) {
            Toast.makeText(this, R.string.eduroam_success, Toast.LENGTH_LONG).show();
            finish();
        } else {
            ((TextView)findViewById(R.id.pin_lock)).setTextColor(0xFFFF0000);
            findViewById(R.id.pin_lock_rem).setVisibility(View.VISIBLE);
        }
    }

    public void onInstallCertificate(View v) {
        Intent intent = new Intent("android.credentials.INSTALL");
        intent.setClassName("com.android.certinstaller", "com.android.certinstaller.CertInstallerMain");
        try {
            InputStream is = getResources().openRawResource(R.raw.rootcert);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(is);
            intent.putExtra(KeyChain.EXTRA_CERTIFICATE, cert.getEncoded());
            intent.putExtra(KeyChain.EXTRA_NAME, "eduroam");
            startActivityForResult(intent, 0);
        } catch (Exception e) {
        }
    }

    public void openSecuritySettings(View v) {
        startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
    }

    public void showDataBackupSettings(View v) {
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    public void onClickCancel(View v) {
        finish();
    }
}