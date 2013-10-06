package de.tum.in.tumcampusapp.activities.wizzard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampusapp.activities.generic.WizzardActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;

public class WizNavExtrasActivity extends WizzardActivity implements
		OnCheckedChangeListener {
	public final static String BROADCAST_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_COLOR_CHANGED";
	public final static String DEFAULT_COLOR_VALUE = "0";

	CheckBox checkBackgroundMode;

	CheckBox checkSilentMode;
	String colorValue = DEFAULT_COLOR_VALUE;
	SharedPreferences preferences;
	RadioButton radioBlue;
	RadioButton radioGray;
	RadioButton radioGreen;

	RadioGroup radioGroup;
	RadioButton radioRed;

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
		String oldColorValue = preferences.getString(Const.COLOR_SCHEME,
				DEFAULT_COLOR_VALUE);

		// Gets the editor for editing preferences and updates the preference
		// values with the choosen state
		Editor editor = preferences.edit();
		editor.putString(Const.COLOR_SCHEME, colorValue);
		editor.putBoolean(Const.SILENCE_SERVICE, checkSilentMode.isChecked());
		editor.putBoolean(Const.BACKGROUND_MODE,
				checkBackgroundMode.isChecked());
		editor.commit();

		// Inform calling activity via broadcast, that the color has changed
		if (!oldColorValue.equals(colorValue)) {
			Intent intentSend = new Intent();
			intentSend.setAction(BROADCAST_NAME);
			sendBroadcast(intentSend);
		}
		startNextActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_wiznav_extras);

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		setIntentForNextActivity(new Intent(this, WizNavDoneActivity.class));
		setIntentForPreviousActivity(new Intent(this,
				WizNavCheckTokenActivity.class));

		radioGroup = (RadioGroup) findViewById(R.id.radiogroupColorScheme);
		radioGroup.setOnCheckedChangeListener(this);

		radioBlue = (RadioButton) findViewById(R.id.radioBlue);
		radioRed = (RadioButton) findViewById(R.id.radioRed);
		radioGreen = (RadioButton) findViewById(R.id.radioGreen);
		radioGray = (RadioButton) findViewById(R.id.radioGray);

		checkSilentMode = (CheckBox) findViewById(R.id.chk_silent_mode);
		checkBackgroundMode = (CheckBox) findViewById(R.id.chk_background_mode);

		colorValue = preferences.getString(Const.COLOR_SCHEME,
				DEFAULT_COLOR_VALUE);
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
		checkSilentMode.setChecked(preferences.getBoolean(
				Const.SILENCE_SERVICE, false));
		checkBackgroundMode.setChecked(preferences.getBoolean(
				Const.BACKGROUND_MODE, false));
	}
}
