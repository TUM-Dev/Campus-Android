package de.tum.in.tumcampus.activities.wizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CheckBox;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.StartupActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;

public class WizNavExtrasActivity extends ActionBarActivity {

    private SharedPreferences preferences;
	private CheckBox checkBackgroundMode;
	private CheckBox checkSilentMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_wiznav_extras);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        checkSilentMode = (CheckBox) findViewById(R.id.chk_silent_mode);
        checkBackgroundMode = (CheckBox) findViewById(R.id.chk_background_mode);

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
     * @param done Done button handle
     */
	@SuppressWarnings("UnusedParameters")
    public void onClickDone(View done) {
		// Gets the editor for editing preferences and updates the preference
		// values with the chosen state
		Editor editor = preferences.edit();
		editor.putBoolean(Const.SILENCE_SERVICE, checkSilentMode.isChecked());
		editor.putBoolean(Const.BACKGROUND_MODE, checkBackgroundMode.isChecked());
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
        if(new AccessTokenManager(this).hasValidAccessToken())
            startActivity(new Intent(this, WizNavCheckTokenActivity.class));
        else
            startActivity(new Intent(this, WizNavStartActivity.class));
    }
}
