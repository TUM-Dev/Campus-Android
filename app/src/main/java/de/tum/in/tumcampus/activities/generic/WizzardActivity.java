package de.tum.in.tumcampus.activities.generic;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

public class WizzardActivity extends ActionBarActivity {
	private Intent intentForNextActivity;
	private Intent intentForPreviousActivity;

	@Override
	public void onBackPressed() {
		startPreviousActivity();
	}

	public void onClickSkip(View view) {
		startNextActivity();
	}

	public void setIntentForNextActivity(Intent intent) {
		this.intentForNextActivity = intent;
	}

	public void setIntentForPreviousActivity(Intent intent) {
		this.intentForPreviousActivity = intent;
	}

	public void startNextActivity() {
		finish();
		if (intentForNextActivity != null) {
			startActivity(intentForNextActivity);
		}
	}

	public void startPreviousActivity() {
		finish();
		if (intentForPreviousActivity != null) {
			startActivity(intentForPreviousActivity);
		}
	}
}
