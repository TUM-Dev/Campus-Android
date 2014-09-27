package de.tum.in.tumcampus.activities.generic;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.UserPreferencesActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequestFetchListener;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Generic class which handles all basic tasks to communicate with TUMOnline and
 * provides a {@link android.support.v7.widget.SearchView} for searching the data.
 * It implements the TUMOnlineRequestFetchListener in order to receive data from
 * TUMOnline and implements a rich user feedback with error progress and token
 * related layouts. Generic class parameter specifies the type of data returned by TumOnline.
 */
public abstract class ActivityForSearchingTumOnline<T> extends ActivityForSearching implements TUMOnlineRequestFetchListener<T>, OnRefreshListener {

	/** The method which should be invoked by the TUmOnline Fetcher */
	private final TUMOnlineConst method;
    private final Class<T> returnClass;

    /** Default layouts for user interaction */
    private RelativeLayout noTokenLayout;
    private RelativeLayout failedTokenLayout;
	protected TUMOnlineRequest<T> requestHandler;

    /**
     * Standard constructor for ActivityForSearchingTumOnline.
     * The given layout must include a progress_layout, failed_layout, no_token_layout and an error_layout.
     * If the Activity should support Pull-To-Refresh it can also contain a
     * {@link uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout} named ptr_layout
     *
     * @param method A identifier specifying what kind of data should be fetched from TumOnline
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     * @param auth Authority for search suggestions declared in manifest file
     * @param minLen Minimum text length that has to be entered by the user before a search quest can be submitted
     *
     */
    public ActivityForSearchingTumOnline(TUMOnlineConst method, Class<T> returnClass, int layoutId, String auth, int minLen) {
        super(layoutId, auth, minLen);
        this.method = method;
        this.returnClass = returnClass;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        failedTokenLayout = (RelativeLayout) findViewById(R.id.failed_layout);
        noTokenLayout = (RelativeLayout) findViewById(R.id.no_token_layout);

        if (noTokenLayout == null || failedTokenLayout == null) {
            Utils.log("Cannot find layouts, did you forget to provide no or failed token layouts?");
        }

        requestHandler = new TUMOnlineRequest<T>(method, returnClass, this, true);
    }

    /**
     * Starts fetching data from TumOnline in background
     * {@link #onLoadFinished(Object)} gets called if data was fetched successfully.
     * If an error occurred it is handled by {@link ActivityForSearchingTumOnline}.
     * */
    protected void requestFetch() {
        requestFetch(false);
    }

    /**
     * Starts fetching data from TumOnline in background
     * {@link #onLoadFinished(Object)} gets called if data was fetched successfully.
     * If an error occurred it is handled by {@link ActivityForSearchingTumOnline}.
     *
     * @param force force reload
     * */
    void requestFetch(boolean force) {
        String accessToken = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.ACCESS_TOKEN, null);
        if (accessToken != null) {
            Log.i(getClass().getSimpleName(), "TUMOnline token is <" + accessToken + ">");
            showLoadingStart();
            requestHandler.setForce(force);
            requestHandler.fetchInteractive(this, this);
        } else {
            Log.i(getClass().getSimpleName(), "No token was set");
            noTokenLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public final void onFetch(T result) {
        noTokenLayout.setVisibility(View.GONE);
        failedTokenLayout.setVisibility(View.GONE);
        showLoadingEnded();

        onLoadFinished(result);
    }

    /**
     * Gets called when fetching data from TumOnline was successful
     * @param result Data from TumOnline
     */
    protected abstract void onLoadFinished(T result);

    /**
     * Handle click on error_layout, failed_layout and no_token_layout
     * @param view Handle of layout view
     */
    @Override
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
        Utils.showToast(this, errorReason);
        failedTokenLayout.setVisibility(View.VISIBLE);
	}

    @Override
    public void onRefreshStarted(View view) {
        requestFetch(true);
    }
}
