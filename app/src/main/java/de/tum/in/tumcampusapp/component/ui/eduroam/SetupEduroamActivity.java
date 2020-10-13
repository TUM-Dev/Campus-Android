package de.tum.in.tumcampusapp.component.ui.eduroam;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Activity that allows the user to easily setup eduroam.
 * Collects all the information needed.
 */
public class SetupEduroamActivity extends BaseActivity {

    private TextInputEditText lrz;
    private TextInputEditText password;

    public SetupEduroamActivity() {
        super(R.layout.activity_setup_eduroam);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra(Const.EXTRA_FOREIGN_CONFIGURATION_EXISTS, false)) {
            showDeleteProfileDialog();
        }

        // Enable 'More Info' links
        ((TextView) findViewById(R.id.text_with_link_2)).setMovementMethod(LinkMovementMethod.getInstance());

        lrz = findViewById(R.id.wifi_lrz_id);
        lrz.setText(Utils.getSetting(this, Const.LRZ_ID, ""));
        password = findViewById(R.id.wifi_password);

        //Set the focus for improved UX experience
        if (lrz.getText() != null && lrz.getText().length() == 0) {
            lrz.requestFocus();
        } else {
            password.requestFocus();
        }

        findViewById(R.id.eduroam_config_error).setOnClickListener(view -> {
            showDeleteProfileDialog();
        });
    }

    private void showDeleteProfileDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.eduroam_dialog_title)
                .setMessage(R.string.eduroam_dialog_info_text)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.eduroam_dialog_preferences, (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                })
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

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
        } else {
            findViewById(R.id.eduroam_config_error).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Open android settingsPrefix
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
