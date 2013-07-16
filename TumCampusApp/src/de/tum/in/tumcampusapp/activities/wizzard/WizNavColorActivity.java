package de.tum.in.tumcampusapp.activities.wizzard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;

public class WizNavColorActivity extends Activity implements
		OnCheckedChangeListener {
	public final static String BROADCAST_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_COLOR_CHANGED";
	public final static String DEFAULT_COLOR_VALUE = "0";

	String colorValue = DEFAULT_COLOR_VALUE;
	RadioButton radioBlue;
	RadioButton radioGray;
	RadioButton radioGreen;
	RadioGroup radioGroup;

	RadioButton radioRed;

	@Override
	public void onBackPressed() {
		finish();
		Intent intent = new Intent(this, WizNavCheckTokenActivity.class);
		startActivity(intent);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radioBlue:
			colorValue = "0";
			break;
		case R.id.radioRed:
			colorValue = "1";
			break;
		case R.id.radioGreen:
			colorValue = "2";
			break;
		case R.id.radioGray:
			colorValue = "3";
			break;
		}
		PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
	}

	public void onClickNext(View view) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		String oldColorValue = sp.getString(Const.COLOR_SCHEME,
				DEFAULT_COLOR_VALUE);

		Editor editor = sp.edit();
		editor.putString(Const.COLOR_SCHEME, colorValue);
		editor.commit();

		// Inform calling activity via broadcast, that the color has changed
		if (!oldColorValue.equals(colorValue)) {
			Intent intentSend = new Intent();
			intentSend.setAction(BROADCAST_NAME);
			sendBroadcast(intentSend);
		}

		finish();
		Intent intent = new Intent(this, WizNavDoneActivity.class);
		startActivity(intent);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_wiznav_color);

		radioGroup = (RadioGroup) findViewById(R.id.radiogroupColorScheme);
		radioGroup.setOnCheckedChangeListener(this);

		radioBlue = (RadioButton) findViewById(R.id.radioBlue);
		radioRed = (RadioButton) findViewById(R.id.radioRed);
		radioGreen = (RadioButton) findViewById(R.id.radioGreen);
		radioGray = (RadioButton) findViewById(R.id.radioGray);

		colorValue = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(Const.COLOR_SCHEME, DEFAULT_COLOR_VALUE);
		if (colorValue != null) {
			if (colorValue.equals("0")) {
				radioBlue.setChecked(true);
			} else if (colorValue.equals("1")) {
				radioRed.setChecked(true);
			} else if (colorValue.equals("2")) {
				radioGreen.setChecked(true);
			} else if (colorValue.equals("3")) {
				radioGray.setChecked(true);
			}
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
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
	}
}
