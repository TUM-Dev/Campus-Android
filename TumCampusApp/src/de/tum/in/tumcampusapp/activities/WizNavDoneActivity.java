package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import de.tum.in.tumcampusapp.R;

public class WizNavDoneActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wiznavdone);
	}

	public void onClickDone(View view) {
		Intent strtActivity = new Intent(this, StartActivity.class);
		startActivity(strtActivity);
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
