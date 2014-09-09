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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;

public class WizNavStartActivity extends ActivityForLoadingInBackground<Void,Boolean> implements OnClickListener {
	private AccessTokenManager accessTokenManager = new AccessTokenManager(this);
	private EditText editText;
	private String lrzId;
	private SharedPreferences sharedPrefs;

    public WizNavStartActivity() {
        super(R.layout.activity_wiznav_start);
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		LinearLayout layout = (LinearLayout) findViewById(R.id.wizstart_layout);
		layout.requestFocus();

		editText = (EditText) findViewById(R.id.lrd_id);

		lrzId = sharedPrefs.getString(Const.LRZ_ID, "");
		if (lrzId != null) {
			editText.setText(lrzId);
		}
	}

    public void onClickSkip(View v) {
        finish();
        startActivity(new Intent(this, WizNavExtrasActivity.class));
    }

    @Override
    protected Boolean onLoadInBackground(Void... arg) {
        return accessTokenManager.requestAccessToken(lrzId);
    }

    @Override
    protected void onLoadFinished(Boolean result) {
        if(result) {
            finish();
            startActivity(new Intent(this, WizNavCheckTokenActivity.class));
        }
    }

    public void onClickNext(View view) {
        lrzId = editText.getText().toString();
        Editor editor = sharedPrefs.edit();
        editor.putString(Const.LRZ_ID, lrzId);
        editor.apply();

		// check if lrz could be valid?
		if (lrzId.length() == AccessTokenManager.MIN_LRZ_LENGTH) {
			// is access token already set?
			if (accessTokenManager.hasValidAccessToken()) {
				// show Dialog first
				new AlertDialog.Builder(this)
				        .setMessage(getString(R.string.dialog_new_token))
						.setPositiveButton(getString(R.string.yes), this)
						.setNegativeButton(getString(R.string.no), this)
						.show();
			} else {
                startLoading();
			}
		} else {
			Toast.makeText(this, getString(R.string.error_lrz_wrong), Toast.LENGTH_LONG).show();
		}
	}

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            startLoading();
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            onLoadFinished(true);
        }
    }
}
