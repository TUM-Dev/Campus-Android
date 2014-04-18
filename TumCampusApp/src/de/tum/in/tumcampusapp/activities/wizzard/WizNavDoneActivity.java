package de.tum.in.tumcampusapp.activities.wizzard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.WizzardActivity;

public class WizNavDoneActivity extends WizzardActivity {

	public void onClickDone(View view) {
		finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_wiznav_done);

		setIntentForNextActivity(null);
		setIntentForPreviousActivity(new Intent(this,
				WizNavExtrasActivity.class));
	}
}
