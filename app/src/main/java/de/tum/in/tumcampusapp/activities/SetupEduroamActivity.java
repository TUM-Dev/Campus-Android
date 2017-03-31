package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.ImplicitCounter;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.CardManager;
import de.tum.in.tumcampusapp.auxiliary.EduroamHelper;

/**
 * Activity that allows the user to easily setup eduroam.
 * Collects all the information needed.
 */
public class SetupEduroamActivity extends BaseActivity {

    private EditText lrz;
    private EditText password;

    public SetupEduroamActivity() {
        super(R.layout.activity_setup_eduroam);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.count(this);
        setContentView(R.layout.activity_setup_eduroam);

        // Enable 'More Info' links
        ((TextView) findViewById(R.id.text_with_link_1)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.text_with_link_2)).setMovementMethod(LinkMovementMethod.getInstance());

        if (Build.VERSION.SDK_INT >= 18) {
            findViewById(R.id.certificate).setVisibility(View.GONE);
        }

        lrz = (EditText) findViewById(R.id.wifi_lrz_id);
        lrz.setText(Utils.getSetting(this, Const.LRZ_ID, ""));
        password = (EditText) findViewById(R.id.wifi_password);
        password.requestFocus();
    }

    /**
     * Start setting up the wifi connection
     *
     * @param v Setup button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickSetup(View v) {
        EduroamHelper manager = new EduroamHelper(getApplicationContext());
        boolean success = manager.configureEduroam(lrz.getText().toString(), password.getText().toString());
        if (success) {
            Utils.showToast(this, R.string.eduroam_success);
            finish();

            CardManager.setShouldRefresh();
        } else {
            ((TextView) findViewById(R.id.pin_lock)).setTextColor(0xFFFF0000);
            findViewById(R.id.pin_lock_rem).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Prompts the user with an install certificate dialog.
     * This is only needed for API level lower than 18.
     * API 18 and above allow automatic installation of certificate
     *
     * @param v Certificate install button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onInstallCertificate(View v) {
        Intent intent = new Intent("android.credentials.INSTALL");
        intent.setClassName("com.android.certinstaller", "com.android.certinstaller.CertInstallerMain");
        try {
            InputStream is = getResources().openRawResource(R.raw.rootcert);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(is);
            intent.putExtra("name", "eduroam");
            intent.putExtra("CERT", cert.getEncoded());
            startActivityForResult(intent, 0);
        } catch (Resources.NotFoundException | CertificateException e) {
            Utils.log(e);
        }
    }

    /**
     * Open security settings
     *
     * @param v Security settings button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void openSecuritySettings(View v) {
        startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
    }

    /**
     * Open android settings
     *
     * @param v Button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void showDataBackupSettings(View v) {
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    @SuppressWarnings("UnusedParameters")
    public void onClickCancel(View v) {
        finish();
    }
}