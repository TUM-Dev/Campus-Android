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
 * Generic class which handles all basic tasks to communicate with TUMOnline. It
 * implements the {@link TUMOnlineRequestFetchListener} in order to receive data from
 * TUMOnline and implements a rich user feedback with error progress and token
 * related layouts.
 */
public abstract class ActivityForAccessingTumOnline extends ProgressActivity implements TUMOnlineRequestFetchListener {

    /**
     * Standard constructor for ActivityForAccessingTumOnline.
     * The given layout must include a progress_layout, failed_layout, no_token_layout and an error_layout.
     * If the Activity should support Pull-To-Refresh it can also contain a
     * {@link SwipeRefreshLayout} named ptr_layout
     *
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     */
    public ActivityForAccessingTumOnline(int layoutId) {
        super(layoutId);
    }

    // TODO: Remove this once TUMOnlineRequestFetchListener is refactored
    @Override
    public void onFetch(Object response) {
        // Ignore
    }

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

    // TODO: Remove
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
        // Subclasses can override this method
    }

    @Override
    public void onNoDataToShow() {
        showError(R.string.no_data_to_show);
    }

}
