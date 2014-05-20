package de.tum.in.tumcampus.activities;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import de.tum.in.tumcampus.R;

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
			packageInfo = getPackageManager().getPackageInfo(getPackageName(),
					0);
			versionName = getResources().getString(R.string.version) + ": "
					+ packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		TextView tv = (TextView) findViewById(R.id.txt_version);
		tv.setText(versionName);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);
		displayVersionName();
		//Counting the number of times that the user used this activity for intelligent reordering 
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getBoolean("implicitly_id", true))
			{
				ImplicitCounter.Counter("information_id",getApplicationContext());
			}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.menu_activity_information, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/* Create the Intent */
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		/* Fill it with Data */
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { getString(R.string.feedbackAddr) });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				getString(R.string.feedbackSubj));

		/* Send it off to the Activity-Chooser */
		startActivity(Intent.createChooser(emailIntent, getResources()
				.getString(R.string.send_email)));
		return true;
	}
}
