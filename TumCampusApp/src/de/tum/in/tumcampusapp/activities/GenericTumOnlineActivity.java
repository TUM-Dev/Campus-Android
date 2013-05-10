package de.tum.in.tumcampusapp.activities;

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

public abstract class GenericTumOnlineActivity extends Activity implements TUMOnlineRequestFetchListener{
	private String accessToken;
	protected RelativeLayout noTokenLayout;
	protected RelativeLayout progressLayout;
	protected RelativeLayout errorLayout;
	protected RelativeLayout failedLayout;
	private TUMOnlineRequest requestHandler;
	private String method;

	public GenericTumOnlineActivity(String method) {
		this.method = method;
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
	}
	
	@Override
	public void onFetchCancelled() {
		finish();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		requestFetch();
	}

	@Override
	public void onFetchError(String errorReason) {
		Log.e(getClass().getSimpleName(), errorReason);
		progressLayout.setVisibility(View.GONE);
		failedLayout.setVisibility(View.VISIBLE);
	}
	
	public void requestFetch() {
		requestHandler = new TUMOnlineRequest(method, this);
		accessToken = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.ACCESS_TOKEN, null);
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
}
