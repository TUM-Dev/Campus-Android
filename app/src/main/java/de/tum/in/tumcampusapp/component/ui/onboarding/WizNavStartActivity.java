package de.tum.in.tumcampusapp.component.ui.onboarding;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.api.tumonline.AccessTokenManager;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Displays the first page of the startup wizard, where the user can enter his lrz-id.
 */
public class WizNavStartActivity extends ActivityForLoadingInBackground<String, Boolean> implements OnClickListener {
    private final AccessTokenManager accessTokenManager = new AccessTokenManager(this);
    private EditText editTxtLrzId;
    private String lrzId;

    public WizNavStartActivity() {
        super(R.layout.activity_wiznav_start);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disableRefresh();
        findViewById(R.id.wizard_start_layout).requestFocus();

        editTxtLrzId = findViewById(R.id.lrz_id);
        editTxtLrzId.setText(Utils.getSetting(this, Const.LRZ_ID, ""));
    }

    /**
     * Handle click on skip button
     *
     * @param skip Skip button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickSkip(View skip) {
        // Upon clicking on the skip button and there is no internet connection -> toast to the user
        if (!NetUtils.isConnected(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_connect_to_internet), Toast.LENGTH_LONG)
                 .show();
            return;
        }

        finish();
        startActivity(new Intent(this, WizNavExtrasActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * Handle click on next button
     *
     * @param next Next button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickNext(View next) {
        // Upon clicking on next button and there is no internet connection -> toast to the user.
        if (!NetUtils.isConnected(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_connect_to_internet), Toast.LENGTH_LONG)
                 .show();
            return;
        }

        String enteredId = editTxtLrzId.getText()
                                       .toString()
                                       .toLowerCase();

        // check if lrz could be valid?
        if (!enteredId.matches(Const.TUM_ID_PATTERN)) {
            Utils.showToast(this, R.string.error_lrz_wrong);
            return;
        }

        lrzId = enteredId;
        Utils.setSetting(this, Const.LRZ_ID, lrzId);

        // is access token already set?
        if (accessTokenManager.hasValidAccessToken()) {
            // show Dialog first
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.dialog_new_token))
                    .setPositiveButton(getString(R.string.yes), this)
                    .setNegativeButton(getString(R.string.no), this)
                    .show();
        } else {
            startLoading(lrzId);
        }
    }

    /**
     * Handle click in dialog buttons
     *
     * @param dialog Dialog handle
     * @param which  Button clicked
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            AuthenticationManager am = new AuthenticationManager(this);
            am.clearKeys();
            am.generatePrivateKey(null);
            startLoading(lrzId);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            onLoadFinished(true);
        }
    }

    private void setDefaultCampus(String facultyNumber) {
        String Campus = "0";
        switch (facultyNumber) {
            case "5":   // TUM School of Education
            case "6":   // Architektur
            case "7":   // Elektrotechnik und Informationstechnik
            case "8":   // Ingenieurfakultät Bau Geo Umwelt
            case "14":  // Wirtschaftswissenschaften
            case "17":  // Andere Einrichtungen
                Campus = "C"; // Stammgelände
                break;

            case "16":  // TUM School of Governance
                // Unklar, nicht weit vom Stammgelände, aber nicht Stammgelände ??
                break;
            case "1":   // Mathematik
            case "2":   // Physik
            case "3":   // Chemie
            case "4":   // Informatik
            case "11":  // Maschinenwesen
                Campus = "G"; // Garching-FZ
                break;

            case "13":  // Sport-und Gesundheitswissenschaften
                // Olympiapark, hat aber keine Zuordnung ??
                break;

            case "12":  // Medizin
                Campus = "I"; // Klinikum rechts der Isar
                break;

            case "15":  // Wissenschaftszentrum Weihenstephan
                Campus = "W"; // Weihenstephan
                break;
            default:
                break;

        }

        if ("0".equals(Campus)) {
            Utils.setSetting(getApplicationContext(), Const.DEFAULT_CAMPUS, Campus);
        }
    }

    /**
     * Requests an access-token from the TumOnline server in background
     *
     * @param arg Unused
     * @return True if the access token was successfully created
     */
    @Override
    protected Boolean onLoadInBackground(String... arg) {
        return accessTokenManager.requestAccessToken(this, arg[0]);
    }

    /**
     * Opens second wizard page if access token available
     *
     * @param result Was access token successfully created
     */
    @Override
    protected void onLoadFinished(Boolean result) {
        if (result) {
            finish();
            startActivity(new Intent(this, WizNavCheckTokenActivity.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        } else {
            showLoadingEnded();
        }
    }
}
