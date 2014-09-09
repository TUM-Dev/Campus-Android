package de.tum.in.tumcampus.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
        ImplicitCounter.Counter(this);
        setContentView(R.layout.activity_information);
        displayVersionName();
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
