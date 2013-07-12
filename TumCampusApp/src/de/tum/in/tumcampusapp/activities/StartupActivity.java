package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import de.tum.in.tumcampusapp.auxiliary.Const;

public class StartupActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent;

		// Workaround for new API version. There was a security update which
		// disallows applications to execute HTTP request in the GUI main
		// thread.
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

		}

		String oldaccesstoken = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getString(Const.ACCESS_TOKEN, "");
		if (oldaccesstoken.length() > 2) {
			intent = new Intent(this, StartActivity.class);
		} else {
			intent = new Intent(this, WizNavStartActivity.class);
		}

		startActivity(intent);
		finish();
	}
}
