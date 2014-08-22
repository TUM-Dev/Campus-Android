package de.tum.in.tumcampus.activities.wizzard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.WizzardActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;

public class WizNavStartActivity extends WizzardActivity implements
        OnClickListener {
	private AccessTokenManager accessTokenManager = new AccessTokenManager(this);
	private EditText editText;
	private String lrzId;
	private SharedPreferences sharedPrefs;

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

		lrzId = sharedPrefs.getString(Const.LRZ_ID, "");
		if (lrzId != null) {
			editText.setText(lrzId);
		}

        // Workaround for new API version. There was a security update which disallows applications to execute HTTP request in the GUI main thread.
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
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
    public void onClickSkip(View v) {
        finish();
        Intent intent = new Intent(this, WizNavExtrasActivity.class);
        startActivity(intent);
    }

	public boolean setupAccessToken() {
		lrzId = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.LRZ_ID, "");
		// check if lrz could be valid?
		if (lrzId.length() == AccessTokenManager.MIN_LRZ_LENGTH) {
			// is access token already set?
			if (accessTokenManager.hasValidAccessToken()) {
				// show Dialog first
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getString(R.string.dialog_new_token))
						.setPositiveButton(getString(R.string.yes), this)
						.setNegativeButton(getString(R.string.no), this)
						.show();
			} else {
				return accessTokenManager.requestAccessToken(lrzId);
			}
		} else {
			Toast.makeText(this, getString(R.string.error_lrz_wrong),
					Toast.LENGTH_LONG).show();
		}
		return false;
	}

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (accessTokenManager.requestAccessToken(lrzId)) {
                startNextActivity();
            }
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            startNextActivity();
        }
    }
}
