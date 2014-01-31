package de.tum.in.tumcampusapp.activities.generic;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.preferences.UserPreferencesActivity;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

/**
 * Generic class which handles all basic tasks to communicate with TUMOnline. It
 * implements the TUMOnlineRequestFetchListener in order to receive data from
 * TUMOnline and implements a rhich user feedback with error progress and token
 * related layouts.
 * 
 * @author Sascha Moecker
 * 
 */
public abstract class ActivityForAccessingTumOnline extends SherlockFragmentActivity
		implements TUMOnlineRequestFetchListener {

	private String accessToken;
	protected RelativeLayout errorLayout;
	protected RelativeLayout failedTokenLayout;
	private int layoutId;
	/** The method which should be invoked by the TUmOnline Fetcher */
	private String method;
	/** Default layouts for user interaction */
	protected RelativeLayout noTokenLayout;
	protected RelativeLayout progressLayout;
	protected TUMOnlineRequest requestHandler;

	public ActivityForAccessingTumOnline(String method, int layoutId) {
		this.method = method;
		this.layoutId = layoutId;
	}

	public void hideErrorLayout() {
		this.errorLayout.setVisibility(View.GONE);
	}

	public void hideProgressLayout() {
		this.progressLayout.setVisibility(View.GONE);
	}

	public void onClick(View view) {
		int viewId = view.getId();
		switch (viewId) {
		case R.id.failed_layout:
			failedTokenLayout.setVisibility(View.GONE);
			requestFetch();
			break;
		case R.id.no_token_layout:
			Intent intent = new Intent(this, UserPreferencesActivity.class);
			startActivity(intent);
			break;
		}
	}

	@Override
	public void onCommonError(String errorReason) {
		Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();
		progressLayout.setVisibility(View.GONE);
		errorLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutId);

		progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
		failedTokenLayout = (RelativeLayout) findViewById(R.id.failed_layout);
		noTokenLayout = (RelativeLayout) findViewById(R.id.no_token_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.error_layout);

		if (progressLayout == null || errorLayout == null
				|| noTokenLayout == null) {
			Log.e(getClass().getSimpleName(),
					"Cannot find layouts, did you forget to provide error and progress layouts?");
		}

		requestHandler = new TUMOnlineRequest(method, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(
				R.menu.menu_activity_for_downloading_external, menu);
		return true;
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
		Log.e(getClass().getSimpleName(), errorReason);
		progressLayout.setVisibility(View.GONE);
		Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();

		// TODO Change errors to Exceptions
		// If there is a failed token layout show this
		if (failedTokenLayout != null) {
			failedTokenLayout.setVisibility(View.VISIBLE);
		} else {
			// Else just use the common error layout
			errorLayout.setVisibility(View.VISIBLE);
		}
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
		accessToken = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(Const.ACCESS_TOKEN, null);
		if (accessToken != null) {
			Log.i(getClass().getSimpleName(), "TUMOnline token is <"
					+ accessToken + ">");
			noTokenLayout.setVisibility(View.GONE);
			progressLayout.setVisibility(View.VISIBLE);
			errorLayout.setVisibility(View.GONE);
			requestHandler.fetchInteractive(this, this);
		} else {
			Log.i(getClass().getSimpleName(), "No token was set");
			noTokenLayout.setVisibility(View.VISIBLE);
		}
	}

	public void showErrorLayout() {
		this.errorLayout.setVisibility(View.VISIBLE);
	}

	public void showProgressLayout() {
		this.progressLayout.setVisibility(View.VISIBLE);
	}
}
