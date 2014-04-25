package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.tum.in.tumcampusapp.R;

/**
 * Provides Information about this app and all contributors
 * 
 * @author Sascha Moecker
 * 
 */
public class InformationActivity extends SherlockActivity {
	/**
	 * Display version name
	 */
	private void displayVersionName() {
		String versionName = "";
		PackageInfo packageInfo;
		try {
			packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			versionName = this.getResources().getString(R.string.version) + ": " + packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		TextView tv = (TextView) this.findViewById(R.id.txt_version);
		tv.setText(versionName);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_information);
		this.displayVersionName();
		// Counting the number of times that the user used this activity for intelligent reordering
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getBoolean("implicitly_id", true)) {
			ImplicitCounter.Counter("information_id", this.getApplicationContext());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		this.getSupportMenuInflater().inflate(R.menu.menu_activity_information, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/* Create the Intent */
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		/* Fill it with Data */
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { this.getString(R.string.feedbackAddr) });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, this.getString(R.string.feedbackSubj));

		/* Send it off to the Activity-Chooser */
		this.startActivity(Intent.createChooser(emailIntent, this.getResources().getString(R.string.send_email)));
		return true;
	}
}
