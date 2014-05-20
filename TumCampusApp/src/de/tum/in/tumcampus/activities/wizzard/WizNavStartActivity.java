package de.tum.in.tumcampus.activities.wizzard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.WizzardActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;

public class WizNavStartActivity extends WizzardActivity implements
		OnClickListener, OnCheckedChangeListener {
	private AccessTokenManager accessTokenManager = new AccessTokenManager(this);
	private CheckBox checkBox;
	private EditText editText;
	private String lrzId;
	private SharedPreferences sharedPrefs;

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Editor editor = sharedPrefs.edit();
		editor.putBoolean(Const.HIDE_WIZZARD_ON_STARTUP, isChecked);
		editor.commit();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			if (accessTokenManager.requestAccessToken(lrzId)) {
				startNextActivity();
			}
		}
		if (which == DialogInterface.BUTTON_NEGATIVE) {
			startNextActivity();
		}
	}

	public void onClickNext(View view) {
		String lrz_id = editText.getText().toString();
		Editor editor = sharedPrefs.edit();
		editor.putString(Const.LRZ_ID, lrz_id);
		editor.commit();

		if (setupAccessToken()) {
			startNextActivity();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_wiznav_start);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		setIntentForNextActivity(new Intent(this,
				WizNavCheckTokenActivity.class));
		setIntentForPreviousActivity(null);

		LinearLayout layout = (LinearLayout) findViewById(R.id.wizstart_layout);
		layout.requestFocus();

		editText = (EditText) findViewById(R.id.lrd_id);
		checkBox = (CheckBox) findViewById(R.id.chk_start_wizzard_on_startup);
		checkBox.requestFocus();

		checkBox.setOnCheckedChangeListener(this);
		checkBox.setChecked(sharedPrefs.getBoolean(
				Const.HIDE_WIZZARD_ON_STARTUP, true));

		// commit the received value to the prefs
		Editor editor = sharedPrefs.edit();
		editor.putBoolean(Const.HIDE_WIZZARD_ON_STARTUP, checkBox.isChecked());
		editor.commit();

		lrzId = sharedPrefs.getString(Const.LRZ_ID, "");
		if (lrzId != null) {
			editText.setText(lrzId);
		}
	}

	public boolean setupAccessToken() {
		lrzId = PreferenceManager.getDefaultSharedPreferences(this).getString(
				Const.LRZ_ID, "");
		// check if lrz could be valid?
		if (lrzId.length() == AccessTokenManager.MIN_LRZ_LENGTH) {
			// is access token already set?
			String oldaccesstoken = PreferenceManager
					.getDefaultSharedPreferences(this).getString(
							Const.ACCESS_TOKEN, "");
			if (oldaccesstoken.length() > 2) {
				// show Dialog first
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(this.getString(R.string.dialog_new_token))
						.setPositiveButton(this.getString(R.string.yes), this)
						.setNegativeButton(this.getString(R.string.no), this)
						.show();
			} else {
				return accessTokenManager.requestAccessToken(lrzId);
			}
		} else {
			Toast.makeText(this, this.getString(R.string.error_lrz_wrong),
					Toast.LENGTH_LONG).show();
		}
		return false;
	}
}
