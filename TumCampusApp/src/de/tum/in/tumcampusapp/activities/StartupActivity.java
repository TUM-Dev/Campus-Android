package de.tum.in.tumcampusapp.activities;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;

public class StartupActivity extends Activity {
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        Intent startIntent;
	        
	        // Workaround for new API version. There was a security update which
			// disallows applications to execute HTTP request in the GUI main
			// thread.
	        if (android.os.Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy);
				
			}
	        String oldaccesstoken = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Const.ACCESS_TOKEN, "");
			if (oldaccesstoken.length() > 2) { 
				startIntent=new Intent(this,StartActivity.class);
			}
			else{
				startIntent=new Intent(this,WizNavStartActivity.class);
			}
			
			startActivity(startIntent);
			finish();
				
	    }
}
