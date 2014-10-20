package de.tum.in.tumcampus.activities.wizard;

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

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Displays the first page of the startup wizard, where the user can enter his lrz-id.
 */
public class WizNavStartActivity extends ActivityForLoadingInBackground<Void,Boolean> implements OnClickListener {
	private final AccessTokenManager accessTokenManager = new AccessTokenManager(this);
	private EditText editText;
	private String lrzId;
	private SharedPreferences sharedPrefs;

    public WizNavStartActivity() {
        super(R.layout.activity_wiznav_start);
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        disableRefresh();

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		LinearLayout layout = (LinearLayout) findViewById(R.id.wizard_start_layout);
		layout.requestFocus();

		editText = (EditText) findViewById(R.id.lrd_id);

		lrzId = sharedPrefs.getString(Const.LRZ_ID, "");
		if (lrzId != null) {
			editText.setText(lrzId);
		}
	}

    /**
     * Handle click on skip button
     * @param skip Skip button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickSkip(View skip) {
        finish();
        if (new AccessTokenManager(this).hasValidAccessToken()) {
            startActivity(new Intent(this, WizNavChatActivity.class));
        } else {
            startActivity(new Intent(this, WizNavExtrasActivity.class));
        }
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * Handle click on next button
     * @param next Next button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickNext(View next) {
        lrzId = editText.getText().toString();
        Editor editor = sharedPrefs.edit();
        editor.putString(Const.LRZ_ID, lrzId);
        editor.apply();

		// check if lrz could be valid?
		if (lrzId.length() >= AccessTokenManager.MIN_LRZ_LENGTH) {
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
			Utils.showToast(this, R.string.error_lrz_wrong);
		}
	}

    /**
     * Handle click in dialog buttons
     * @param dialog Dialog handle
     * @param which Button clicked
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            startLoading();
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            onLoadFinished(true);
        }
    }

    /**
     * Requests an access-token from the TumOnline server in background
     * @param arg Unused
     * @return True if the access token was successfully created
     */
    @Override
    protected Boolean onLoadInBackground(Void... arg) {
        return accessTokenManager.requestAccessToken(WizNavStartActivity.this, lrzId);
    }

    /**
     * Opens second wizard page if access token available
     * @param result Was access token successfully created
     */
    @Override
    protected void onLoadFinished(Boolean result) {
        if(result) {
            finish();
            startActivity(new Intent(this, WizNavCheckTokenActivity.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        } else {
            showLoadingEnded();
        }
    }
}
