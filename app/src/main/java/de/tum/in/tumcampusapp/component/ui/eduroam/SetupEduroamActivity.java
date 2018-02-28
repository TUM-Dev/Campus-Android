package de.tum.in.tumcampusapp.component.ui.eduroam;

import android.app.AlertDialog;
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
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.other.reporting.stats.ImplicitCounter;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

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

        if (getIntent().getBooleanExtra(Const.EXTRA_FOREIGN_CONFIGURATION_EXISTS, false)) {
            showDeleteProfileDialog(true);
        }

        // Enable 'More Info' links
        ((TextView) findViewById(R.id.text_with_link_2)).setMovementMethod(LinkMovementMethod.getInstance());

        if (Build.VERSION.SDK_INT >= 18) {
            findViewById(R.id.certificate).setVisibility(View.GONE);
        }

        lrz = findViewById(R.id.wifi_lrz_id);
        lrz.setText(Utils.getSetting(this, Const.LRZ_ID, ""));
        password = findViewById(R.id.wifi_password);

        //Set the focus for improved UX experience
        if (lrz.getText()
               .length() == 0) {
            lrz.requestFocus();
        } else {
            password.requestFocus();
        }

        findViewById(R.id.eduroam_config_error).setOnClickListener(view -> {
            showDeleteProfileDialog(false);
        });
    }

    private void showDeleteProfileDialog(boolean showAtStart) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.eduroam_dialog_title);
        View content = getLayoutInflater().inflate(R.layout.delete_wifi_config, null);
        content.findViewById(R.id.button_open_wifi_preferences)
               .setOnClickListener(view1 -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        if (showAtStart) {
            content.findViewById(R.id.eduroam_delete_info)
                   .setVisibility(View.VISIBLE);
        } else {
            content.findViewById(R.id.eduroam_delete_info)
                   .setVisibility(View.GONE);
        }
        dialog.setView(content);
        dialog.setPositiveButton(R.string.done, null);
        dialog.show();
    }

    /**
     * Start setting up the wifi connection
     *
     * @param v Setup button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickSetup(View v) {
        //Verify that we have a valid LRZ / TUM ID
        final Pattern pattern = Pattern.compile(Const.TUM_ID_PATTERN);
        if (!pattern.matcher(lrz.getText())
                    .matches()) {
            Utils.showToast(this, getString(R.string.eduroam_not_valid_id));
            return;
        }

        //We need some sort of password
        if (password.getText()
                    .length() == 0) {
            Utils.showToast(this, getString(R.string.eduroam_please_enter_password));
            return;
        }

        //Do Setup
        EduroamController manager = new EduroamController(getApplicationContext());
        boolean success = manager.configureEduroam(lrz.getText()
                                                      .toString(), password.getText()
                                                                           .toString());
        if (success) {
            Utils.showToast(this, R.string.eduroam_success);
            Utils.setSetting(this, Const.REFRESH_CARDS, true);
            finish();

            CardManager.setShouldRefresh();
        } else {
            findViewById(R.id.eduroam_config_error).setVisibility(View.VISIBLE);
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
        Utils.setSetting(this, Const.REFRESH_CARDS, true);
        finish();
    }
}