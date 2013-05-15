package de.tum.in.tumcampusapp.activities.generic;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.preferences.UserPreferencesActivity;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

public abstract class ActivityForAccessingTumOnline extends Activity implements TUMOnlineRequestFetchListener {
	public final static int MENU_REFRESH = 0;

	private String accessToken;
	protected RelativeLayout errorLayout;
	protected RelativeLayout failedLayout;
	private int layoutId;
	/** The method which should be invoked by the TUmOnline Fetcher */
	private String method;
	/** Default layouts for user interaction */
	protected RelativeLayout noTokenLayout;
	protected RelativeLayout progressLayout;
	protected TUMOnlineRequest requestHandler;

	public ActivityForAccessingTumOnline(String method, int layoutIt) {
		this.method = method;
		this.layoutId = layoutIt;
	}

	public void onClick(View view) {
		int viewId = view.getId();
		switch (viewId) {
		case R.id.failed_layout:
			failedLayout.setVisibility(View.GONE);
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
		failedLayout = (RelativeLayout) findViewById(R.id.failed_layout);
		noTokenLayout = (RelativeLayout) findViewById(R.id.no_token_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.error_layout);

		requestHandler = new TUMOnlineRequest(method, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem m = menu.add(0, MENU_REFRESH, 0, getString(R.string.update));
		m.setIcon(android.R.drawable.ic_menu_rotate);
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
		// TODO Change errors to Exceptions
		if (failedLayout != null) {
			failedLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			requestFetch();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void requestFetch() {
		accessToken = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.ACCESS_TOKEN, null);
		if (accessToken != null) {
			Log.i(getClass().getSimpleName(), "TUMOnline token is <" + accessToken + ">");
			noTokenLayout.setVisibility(View.GONE);
			progressLayout.setVisibility(View.VISIBLE);
			errorLayout.setVisibility(View.GONE);
			requestHandler.fetchInteractive(this, this);
		} else {
			Log.i(getClass().getSimpleName(), "No token was set");
			noTokenLayout.setVisibility(View.VISIBLE);
		}
	}
}
