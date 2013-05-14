package de.tum.in.tumcampusapp.activities.generic;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.preferences.UserPreferencesActivity;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

public abstract class TumOnlineActivity extends Activity implements TUMOnlineRequestFetchListener {
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

	public TumOnlineActivity(String method, int layoutIt) {
		this.method = method;
		this.layoutId = layoutIt;
	}

	public void onClick(View view) {
		int viewId = view.getId();
		switch (viewId) {
		case R.id.failed_layout:
			failedLayout.setVisibility(View.GONE);
			requestFetchRequiresToken();
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

	public void requestFetch() {
		progressLayout.setVisibility(View.VISIBLE);
		errorLayout.setVisibility(View.GONE);
		requestHandler.fetchInteractive(this, this);
	}

	public void requestFetchRequiresToken() {
		accessToken = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.ACCESS_TOKEN, null);
		if (accessToken != null) {
			Log.i(getClass().getSimpleName(), "TUMOnline token is <" + accessToken + ">");
			noTokenLayout.setVisibility(View.GONE);
			requestFetch();
		} else {
			Log.i(getClass().getSimpleName(), "No token was set");
			noTokenLayout.setVisibility(View.VISIBLE);
		}
	}
}
