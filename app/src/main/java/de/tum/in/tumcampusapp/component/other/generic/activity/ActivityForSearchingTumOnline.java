package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.exception.NoNetworkConnectionException;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineResponseListener;
import de.tum.in.tumcampusapp.api.tumonline.exception.InactiveTokenException;
import de.tum.in.tumcampusapp.api.tumonline.exception.InvalidTokenException;
import de.tum.in.tumcampusapp.api.tumonline.exception.MissingPermissionException;
import de.tum.in.tumcampusapp.api.tumonline.exception.RequestLimitReachedException;
import de.tum.in.tumcampusapp.api.tumonline.exception.TokenLimitReachedException;
import de.tum.in.tumcampusapp.api.tumonline.exception.UnknownErrorException;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Generic class which handles all basic tasks to communicate with TUMOnline and
 * provides a {@link android.support.v7.widget.SearchView} for searching the data.
 * It implements the TUMOnlineRequestFetchListener in order to receive data from
 * TUMOnline and implements a rich user feedback with error progress and token
 * related layouts. Generic class parameter specifies the type of data returned by TumOnline.
 */
public abstract class ActivityForSearchingTumOnline extends ActivityForSearching {

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

    // TODO: Remove duplicated code with ActivityForAccessingTumOnline

    protected <T> void fetch(Call<T> call, TUMOnlineResponseListener<T> listener) {
        showLoadingStart();
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                showLoadingEnded();
                T body = response.body();
                if (response.isSuccessful() && body != null) {
                    listener.onDownloadSuccessful(body);
                } else if (body == null) {
                    onEmptyDownloadResponse();
                } else {
                    onDownloadUnsuccessful(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                showLoadingEnded();
                onDownloadFailure(t);
            }
        });
    }

    protected final void onEmptyDownloadResponse() {
        // TODO
    }

    protected final void onDownloadUnsuccessful(int statusCode) {
        if (statusCode == 503) {
            // The service is unavailable
            showError(R.string.error_tum_online_unavailable);
        } else {
            showError(R.string.error_unknown);
        }
    }

    protected final void onDownloadFailure(@NonNull Throwable throwable) {
        Utils.log(throwable);
        showLoadingEnded();

        if (throwable instanceof NoNetworkConnectionException) {
            showNoInternetLayout();
        } else if (throwable instanceof InactiveTokenException) {
            String message = getString(R.string.error_access_token_inactive);
            showFailedTokenLayout(message);
        } else if (throwable instanceof InvalidTokenException) {
            showNoTokenLayout();
        } else if (throwable instanceof MissingPermissionException) {
            String message = getString(R.string.error_no_rights_to_access_function);
            showFailedTokenLayout(message);
        } else if (throwable instanceof TokenLimitReachedException) {
            String message = getString(R.string.error_access_token_limit_reached);
            showFailedTokenLayout(message);
        } else if (throwable instanceof RequestLimitReachedException) {
            String message = getString(R.string.error_request_limit_reached);
            showFailedTokenLayout(message);
        } else if (throwable instanceof UnknownErrorException) {
            showError(R.string.error_unknown);
        }
    }

    /*
    // TODO TILL: Refactor like ActivityForAccessingTumOnline (maybe this should be superclass?)

    protected final void handleDownloadUnsuccessful(int statusCode) {
        if (statusCode == 503) {
            // The service is unavailable
            showError(R.string.error_tum_online_unavailable);
        } else {
            showError(R.string.error_unknown);
        }
    }

    protected final void handleDownloadError(Throwable throwable) {
        Utils.log(throwable);
        showLoadingEnded();

        if (throwable instanceof NoNetworkConnectionException) {
            showNoInternetLayout();
        } else if (throwable instanceof InactiveTokenException) {
            String message = getString(R.string.error_access_token_inactive);
            showFailedTokenLayout(message);
        } else if (throwable instanceof InvalidTokenException) {
            showNoTokenLayout();
        } else if (throwable instanceof MissingPermissionException) {
            String message = getString(R.string.error_no_rights_to_access_function);
            showFailedTokenLayout(message);
        } else if (throwable instanceof TokenLimitReachedException) {
            String message = getString(R.string.error_access_token_limit_reached);
            showFailedTokenLayout(message);
        } else if (throwable instanceof RequestLimitReachedException) {
            String message = getString(R.string.error_request_limit_reached);
            showFailedTokenLayout(message);
        } else if (throwable instanceof UnknownErrorException) {
            showError(R.string.error_unknown);
        }
    }
    */

}
