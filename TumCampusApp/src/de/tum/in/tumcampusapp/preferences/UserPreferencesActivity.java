package de.tum.in.tumcampusapp.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.WizNavStartActivity;
import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;
import de.tum.in.tumcampusapp.auxiliary.Const;

public class UserPreferencesActivity extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private AccessTokenManager accessTokenManager = new AccessTokenManager(this);

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		// Click listener for preference list entries. Used to simulate a button
		// (since it is not possible to add a button to the preferences screen)
		Preference buttonToken = (Preference) findPreference("button_token");
		buttonToken
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						// Querys for a access token from TUMOnline
						accessTokenManager.setupAccessToken();
						return true;
					}
				});
		Preference buttonWizzard = (Preference) findPreference("button_wizzard");
		buttonWizzard
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						finish();
						Intent intent = new Intent(
								UserPreferencesActivity.this,
								WizNavStartActivity.class);
						startActivity(intent);
						return true;
					}
				});
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// if the color scheme has changed, fire a on activity result intent
		// which can be used by the calling activity
		if (key.equals("color_scheme")) {
			Intent returnIntent = new Intent();
			returnIntent.putExtra(Const.PREFS_HAVE_CHANGED, true);
			setResult(RESULT_OK, returnIntent);
		}
	}
}