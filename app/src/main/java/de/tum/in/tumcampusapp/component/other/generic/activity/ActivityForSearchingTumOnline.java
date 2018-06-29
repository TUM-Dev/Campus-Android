package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.support.v4.widget.SwipeRefreshLayout;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.exception.NoNetworkConnectionException;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequestFetchListener;
import de.tum.in.tumcampusapp.api.tumonline.exception.InactiveTokenException;
import de.tum.in.tumcampusapp.api.tumonline.exception.InvalidTokenException;
import de.tum.in.tumcampusapp.api.tumonline.exception.MissingPermissionException;
import de.tum.in.tumcampusapp.api.tumonline.exception.RequestLimitReachedException;
import de.tum.in.tumcampusapp.api.tumonline.exception.TokenLimitReachedException;
import de.tum.in.tumcampusapp.api.tumonline.exception.UnknownErrorException;
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
     * Standard constructor for ActivityForSearchingTumOnline.
     * The given layout must include a all_error_layout.
     * If the Activity should support Pull-To-Refresh it can also contain a
     * {@link SwipeRefreshLayout} named ptr_layout
     *
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     * @param auth     Authority for search suggestions declared in manifest file
     * @param minLen   Minimum text length that has to be entered by the user before a search quest can be submitted
     */
    public ActivityForSearchingTumOnline(int layoutId, String auth, int minLen) {
        super(layoutId, auth, minLen);
    }

    /**
     * Starts fetching data from TumOnline in background
     * {@link #onLoadFinished(Object)} gets called if data was fetched successfully.
     * If an error occurred it is handled by {@link ActivityForSearchingTumOnline}.
     */
    /*
    protected void requestFetch() {
        requestFetch(false);
    }
    */

    /**
     * Starts fetching data from TumOnline in background
     * {@link #onLoadFinished(Object)} gets called if data was fetched successfully.
     * If an error occurred it is handled by {@link ActivityForSearchingTumOnline}.
     */
    /*
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
    */

    @Override
    public final void onFetch(T result) {
        // TODO: Remove
    }

    // TODO: Duplicated code - fix it!
    protected final void handleDownloadError(Throwable throwable) {
        Utils.log(throwable);
        showLoadingEnded();

        if (throwable instanceof NoNetworkConnectionException) {
            showNoInternetLayout();
        } else if (throwable instanceof InactiveTokenException) {
            String message = getString(R.string.dialog_access_token_invalid);
            showFailedTokenLayout(message);
        } else if (throwable instanceof InvalidTokenException) {
            showNoTokenLayout();
        } else if (throwable instanceof MissingPermissionException) {
            String message = getString(R.string.dialog_no_rights_function);
            showFailedTokenLayout(message);
        } else if (throwable instanceof TokenLimitReachedException) {
            String message = getString(R.string.token_limit_reached);
            showFailedTokenLayout(message);
        } else if (throwable instanceof RequestLimitReachedException) {
            String message = getString(R.string.request_limit_reached);
            showFailedTokenLayout(message);
        } else if (throwable instanceof UnknownErrorException) {
            showError(R.string.exception_unknown);
        }
    }

    /**
     * Gets called when fetching data from TumOnline was successful
     *
     * @param result Data from TumOnline
     */
    protected void onLoadFinished(T result) {
        // TODO: Remove
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

    /*
    @Override
    public void onRefresh() {
        requestFetch(true);
    }
    */

    @Override
    public void onNoDataToShow() {
        showError(R.string.no_data_to_show);
    }
}
