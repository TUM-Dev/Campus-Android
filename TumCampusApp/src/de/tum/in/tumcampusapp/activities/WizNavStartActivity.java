package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.WizAccessTokenManager;

public class WizNavStartActivity extends Activity {
	private WizAccessTokenManager accessTokenManager = new WizAccessTokenManager(this);

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wiznavstart);
	}

	public void onClickNext(View view) {
		EditText editText = (EditText) findViewById(R.id.lrd_id);
		String lrz_id = editText.getText().toString();

		accessTokenManager.setupAccessToken(lrz_id);

		if (accessTokenManager.isFine) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			Editor editor = sp.edit();
			editor.putString("lrz_id", lrz_id);
			editor.commit();
			Intent wizNav = new Intent(this, WizNavNextActivity.class);
			startActivity(wizNav);
		} else {
			TextView tv = (TextView) findViewById(R.id.textViewErr);
			tv.setText(accessTokenManager.message);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity_wizzard, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_exit:
			finish();
			Intent startAct = new Intent(this, StartActivity.class);
			startActivity(startAct);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
