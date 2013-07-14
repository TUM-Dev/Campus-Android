package de.tum.in.tumcampusapp.activities.wizzard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;

public class WizNavStartActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener {
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
				startWizNavNextActivity();
			}
		}
		if (which == DialogInterface.BUTTON_NEGATIVE) {
			startWizNavNextActivity();
		}
	}

	public void onClickNext(View view) {

		String lrz_id = editText.getText().toString();
		Editor editor = sharedPrefs.edit();
		editor.putString(Const.LRZ_ID, lrz_id);
		editor.commit();

		if (setupAccessToken()) {
			startWizNavNextActivity();
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_wiznav_start);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

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

	public void startWizNavNextActivity() {
		finish();
		Intent intent = new Intent(this, WizNavCheckTokenActivity.class);
		startActivity(intent);
	}
}
