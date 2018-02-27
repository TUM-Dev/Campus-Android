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
 * Generic class which handles all basic tasks to communicate with TUMOnline. It
 * implements the {@link TUMOnlineRequestFetchListener} in order to receive data from
 * TUMOnline and implements a rich user feedback with error progress and token
 * related layouts.
 */
public abstract class ActivityForAccessingTumOnline<T> extends ProgressActivity implements TUMOnlineRequestFetchListener<T> {

    /**
     * The method which should be invoked by the TumOnlineFetcher
     */
    private final TUMOnlineConst<T> method;

    /**
     * Default layouts for user interaction
     */
    protected TUMOnlineRequest<T> requestHandler;

    /**
     * Standard constructor for ActivityForAccessingTumOnline.
     * The given layout must include a progress_layout, failed_layout, no_token_layout and an error_layout.
     * If the Activity should support Pull-To-Refresh it can also contain a
     * {@link SwipeRefreshLayout} named ptr_layout
     *
     * @param method   A identifier specifying what kind of data should be fetched from TumOnline
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     */
    public ActivityForAccessingTumOnline(TUMOnlineConst<T> method, int layoutId) {
        super(layoutId);
        this.method = method;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestHandler = new TUMOnlineRequest<>(method, this, true);
    }

    /**
     * Starts fetching data from TumOnline in background
     * {@link #onFetch(T)} gets called if data was fetched successfully.
     * If an error occurred it is handled by {@link ActivityForAccessingTumOnline}.
     */
    protected void requestFetch() {
        requestFetch(false);
    }

    /**
     * Starts fetching data from TumOnline in background
     * {@link #onFetch(T)} gets called if data was fetched successfully.
     * If an error occurred it is handled by {@link ActivityForAccessingTumOnline}.
     *
     * @param force Force reload of content
     */
    void requestFetch(boolean force) {
        String accessToken = PreferenceManager.getDefaultSharedPreferences(this)
                                              .getString(Const.ACCESS_TOKEN, null);
        if (accessToken == null) {
            showNoTokenLayout();
        } else {
            Utils.logv("TUMOnline token is <" + accessToken + ">");
            showLoadingStart();
            requestHandler.setForce(force);
            requestHandler.fetchInteractive(this, this);
        }
    }

    @Override
    public void onNoInternetError() {
        showNoInternetLayout();
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
