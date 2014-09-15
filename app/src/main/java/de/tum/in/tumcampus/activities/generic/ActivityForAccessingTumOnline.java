package de.tum.in.tumcampus.activities.generic;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.UserPreferencesActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequestFetchListener;

/**
 * Generic class which handles all basic tasks to communicate with TUMOnline. It
 * implements the TUMOnlineRequestFetchListener in order to receive data from
 * TUMOnline and implements a rhich user feedback with error progress and token
 * related layouts.
 * 
 * @author Sascha Moecker
 * 
 */
public abstract class ActivityForAccessingTumOnline extends ProgressActivity implements TUMOnlineRequestFetchListener {

	/** The method which should be invoked by the TUmOnline Fetcher */
	private String method;

	/** Default layouts for user interaction */
	protected RelativeLayout noTokenLayout;
    protected RelativeLayout failedTokenLayout;
	protected TUMOnlineRequest requestHandler;

	public ActivityForAccessingTumOnline(String method, int layoutId) {
        super(layoutId);
        this.method = method;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        failedTokenLayout = (RelativeLayout) findViewById(R.id.failed_layout);
        noTokenLayout = (RelativeLayout) findViewById(R.id.no_token_layout);

        if (noTokenLayout == null || failedTokenLayout == null) {
            Log.e(getClass().getSimpleName(), "Cannot find layouts, did you forget to provide no or failed token layouts?");
        }

        requestHandler = new TUMOnlineRequest(method, this, true);
    }

    public void requestFetch() {
        String accessToken = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.ACCESS_TOKEN, null);
        if (accessToken != null) {
            Log.i(getClass().getSimpleName(), "TUMOnline token is <" + accessToken + ">");
            showLoadingStart();
            requestHandler.fetchInteractive(this, this);
        } else {
            Log.i(getClass().getSimpleName(), "No token was set");
            noTokenLayout.setVisibility(View.VISIBLE);
        }
    }

	public void onClick(View view) {
		int viewId = view.getId();
		switch (viewId) {
		case R.id.failed_layout:
        case R.id.error_layout:
			requestFetch();
			break;
		case R.id.no_token_layout:
			startActivity(new Intent(this, UserPreferencesActivity.class));
			break;
		}
	}

	@Override
	public void onCommonError(String errorReason) {
		showError(errorReason);
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        requestHandler.cancelRequest(true);
    }

    @Override
	public void onFetchCancelled() {
		finish();
	}

	@Override
	public void onFetchError(String errorReason) {
		showLoadingEnded();
		Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();
        failedTokenLayout.setVisibility(View.VISIBLE);
	}
}
