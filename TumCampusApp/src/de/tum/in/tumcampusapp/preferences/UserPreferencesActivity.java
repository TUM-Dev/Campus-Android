package de.tum.in.tumcampusapp.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;

public class UserPreferencesActivity extends PreferenceActivity {
	private AccessTokenManager accessTokenManager = new AccessTokenManager(this);

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		// Click listener for preference list entrys. Used to simulate a button
		// (since it is not possible to add a button to the preferences screen)
		Preference button = (Preference) findPreference("button");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				accessTokenManager.setupAccessToken();
				// TODO new SetupAccessToken().execute();
				return true;
			}
		});
	}
}
