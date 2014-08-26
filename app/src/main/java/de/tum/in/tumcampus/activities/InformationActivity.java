package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;

/**
 * Provides Information about this app and all contributors
 * 
 * @author Sascha Moecker
 * 
 */
public class InformationActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        displayVersionName();
        //Counting the number of times that the user used this activity for intelligent reordering
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs.getBoolean("implicitly_id", true))
        {
            ImplicitCounter.Counter("information_id", getApplicationContext());
        }
    }

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
}
