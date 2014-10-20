package de.tum.in.tumcampus.activities.wizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.StartupActivity;
import de.tum.in.tumcampus.activities.generic.BaseActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;

public class WizNavExtrasActivity extends BaseActivity {

    private SharedPreferences preferences;
    private CheckBox checkBackgroundMode;
    private CheckBox checkSilentMode;
    private CheckBox bugReport;
    private boolean tokenSetup = false;

    public WizNavExtrasActivity() {
        super(R.layout.activity_wiznav_extras);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // If called because app version changed remove "Step 4" and close on back pressed
        Intent i = getIntent();
        if (i != null && i.hasExtra(Const.TOKEN_IS_SETUP)) {
            tokenSetup = i.getBooleanExtra(Const.TOKEN_IS_SETUP, false);
            findViewById(R.id.step_4).setVisibility(View.GONE);
        }

        // Get handles to all UI elements
        checkSilentMode = (CheckBox) findViewById(R.id.chk_silent_mode);
        checkBackgroundMode = (CheckBox) findViewById(R.id.chk_background_mode);
        bugReport = (CheckBox) findViewById(R.id.chk_bug_reports);

        // Only make silent service selectable if access token exists
        // Otherwise the app cannot load lectures so silence service makes no sense
        if (new AccessTokenManager(this).hasValidAccessToken()) {
            checkSilentMode.setChecked(preferences.getBoolean(Const.SILENCE_SERVICE, true));
        } else {
            checkSilentMode.setChecked(false);
            checkSilentMode.setEnabled(false);
        }
        checkBackgroundMode.setChecked(preferences.getBoolean(Const.BACKGROUND_MODE, true));
    }

    /**
     * Set preference values and open {@link StartupActivity}
     *
     * @param done Done button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickDone(View done) {
        // Gets the editor for editing preferences and
        // updates the preference values with the chosen state
        Editor editor = preferences.edit();
        editor.putBoolean(Const.SILENCE_SERVICE, checkSilentMode.isChecked());
        editor.putBoolean(Const.BACKGROUND_MODE, checkBackgroundMode.isChecked());
        editor.putBoolean(Const.BUG_REPORTS, bugReport.isChecked());
        editor.putBoolean(Const.HIDE_WIZARD_ON_STARTUP, true);
        editor.apply();

        finish();
        startActivity(new Intent(this, StartupActivity.class));
    }

    /**
     * If back key is pressed, open previous activity
     */
    @Override
    public void onBackPressed() {
        finish();
        Intent intent = null;
        if (tokenSetup && new AccessTokenManager(this).hasValidAccessToken())
            intent = new Intent(this, WizNavChatActivity.class);
        else if (!tokenSetup)
            intent = new Intent(this, WizNavStartActivity.class);

        if (intent != null) {
            intent.putExtra(Const.TOKEN_IS_SETUP, tokenSetup);
            startActivity(intent);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
    }
}
