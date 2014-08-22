package de.tum.in.tumcampus.activities.wizzard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.WizzardActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;

public class WizNavExtrasActivity extends WizzardActivity {

	CheckBox checkBackgroundMode;

	CheckBox checkSilentMode;
	SharedPreferences preferences;

	public void onClickDone(View view) {
		// Gets the editor for editing preferences and updates the preference
		// values with the choosen state
		Editor editor = preferences.edit();
		editor.putBoolean(Const.SILENCE_SERVICE, checkSilentMode.isChecked());
		editor.putBoolean(Const.BACKGROUND_MODE, checkBackgroundMode.isChecked());
        editor.putBoolean(Const.HIDE_WIZZARD_ON_STARTUP, true);
		editor.commit();

		finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_wiznav_extras);

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		setIntentForNextActivity(null);
		setIntentForPreviousActivity(new Intent(this,
				WizNavCheckTokenActivity.class));

		checkSilentMode = (CheckBox) findViewById(R.id.chk_silent_mode);
		checkBackgroundMode = (CheckBox) findViewById(R.id.chk_background_mode);

        // Only make silent service selectable if access token exists
        // Otherwise the app cannot load lectures so silence service makes no sense
        if (new AccessTokenManager(this).hasValidAccessToken()) {
            checkSilentMode.setChecked(preferences.getBoolean(
                    Const.SILENCE_SERVICE, false));
        } else {
            checkSilentMode.setChecked(false);
            checkSilentMode.setEnabled(false);
        }
		checkBackgroundMode.setChecked(preferences.getBoolean(
				Const.BACKGROUND_MODE, false));
	}
}
