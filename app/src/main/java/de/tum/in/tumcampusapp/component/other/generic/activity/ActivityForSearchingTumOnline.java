package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequestFetchListener;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Generic class which handles all basic tasks to communicate with TUMOnline and
 * provides a {@link android.support.v7.widget.SearchView} for searching the data.
 * It implements the TUMOnlineRequestFetchListener in order to receive data from
 * TUMOnline and implements a rich user feedback with error progress and token
 * related layouts. Generic class parameter specifies the type of data returned by TumOnline.
 */
public abstract class ActivityForSearchingTumOnline<T> extends ActivityForSearching implements TUMOnlineRequestFetchListener<T> {

    /**
     * The method which should be invoked by the TUmOnline Fetcher
     */
    private final TUMOnlineConst<T> method;

    /**
     * Default layouts for user interaction
     */
    protected TUMOnlineRequest<T> requestHandler;

    /**
     * Standard constructor for ActivityForSearchingTumOnline.
     * The given layout must include a all_error_layout.
     * If the Activity should support Pull-To-Refresh it can also contain a
     * {@link SwipeRefreshLayout} named ptr_layout
     *
     * @param method   A identifier specifying what kind of data should be fetched from TumOnline
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     * @param auth     Authority for search suggestions declared in manifest file
     * @param minLen   Minimum text length that has to be entered by the user before a search quest can be submitted
     */
    public ActivityForSearchingTumOnline(TUMOnlineConst<T> method, int layoutId, String auth, int minLen) {
        super(layoutId, auth, minLen);
        this.method = method;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestHandler = new TUMOnlineRequest<>(method, this, true);
    }

    /**
     * Starts fetching data from TumOnline in background
     * {@link #onLoadFinished(Object)} gets called if data was fetched successfully.
     * If an error occurred it is handled by {@link ActivityForSearchingTumOnline}.
     */
    protected void requestFetch() {
        requestFetch(false);
    }

    /**
     * Starts fetching data from TumOnline in background
     * {@link #onLoadFinished(Object)} gets called if data was fetched successfully.
     * If an error occurred it is handled by {@link ActivityForSearchingTumOnline}.
     *
     * @param force force reload
     */
    void requestFetch(boolean force) {
        String accessToken = PreferenceManager.getDefaultSharedPreferences(this)
                                              .getString(Const.ACCESS_TOKEN, null);
        if (accessToken == null) {
            showNoTokenLayout();
            return;
        }
        Utils.logv("TUMOnline token is <" + accessToken + ">");
        showLoadingStart();
        requestHandler.setForce(force);
        requestHandler.fetchInteractive(this, this);

    }

    @Override
    public final void onFetch(T result) {
        showLoadingEnded();

        onLoadFinished(result);
    }

    /**
     * Gets called when fetching data from TumOnline was successful
     *
     * @param result Data from TumOnline
     */
    protected abstract void onLoadFinished(T result);

    @Override
    public void onNoInternetError() {
        showNoInternetLayout();
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
        showFailedTokenLayout(errorReason);
    }

    @Override
    public void onRefresh() {
        requestFetch(true);
    }

    @Override
    public void onNoDataToShow() {
        showError(R.string.no_data_to_show);
    }
}
