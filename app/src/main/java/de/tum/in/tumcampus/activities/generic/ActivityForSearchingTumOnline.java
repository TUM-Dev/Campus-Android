package de.tum.in.tumcampus.activities.generic;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
 */
public abstract class ActivityForSearchingTumOnline extends ActivityForSearching implements TUMOnlineRequestFetchListener {

	/** The method which should be invoked by the TUmOnline Fetcher */
	private String method;

	/** Default layouts for user interaction */
	protected RelativeLayout noTokenLayout;
    protected RelativeLayout failedTokenLayout;
	protected TUMOnlineRequest requestHandler;

	public ActivityForSearchingTumOnline(String method, int layoutId, String auth, int minLen) {
        super(layoutId, auth, minLen);
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

        requestHandler = new TUMOnlineRequest(method, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update:
                requestFetch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void requestFetch() {
        String accessToken = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.ACCESS_TOKEN, null);
        if (accessToken != null) {
            Log.i(getClass().getSimpleName(), "TUMOnline token is <" + accessToken + ">");
            noTokenLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
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
			failedTokenLayout.setVisibility(View.GONE);
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
		progressLayout.setVisibility(View.GONE);
		Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();

		// If there is a failed token layout show this
		if (failedTokenLayout != null) {
			failedTokenLayout.setVisibility(View.VISIBLE);
		} else {
			// Else just use the common error layout
			errorLayout.setVisibility(View.VISIBLE);
		}
	}
}
