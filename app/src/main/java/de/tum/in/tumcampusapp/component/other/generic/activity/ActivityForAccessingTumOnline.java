package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.exception.NoNetworkConnectionException;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.api.tumonline.exception.InactiveTokenException;
import de.tum.in.tumcampusapp.api.tumonline.exception.InvalidTokenException;
import de.tum.in.tumcampusapp.api.tumonline.exception.MissingPermissionException;
import de.tum.in.tumcampusapp.api.tumonline.exception.RequestLimitReachedException;
import de.tum.in.tumcampusapp.api.tumonline.exception.TokenLimitReachedException;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This Activity can be extended by concrete Activities that access information from TUMonline. It
 * includes methods for fetching content (both via {@link TUMOnlineClient} and from the local
 * cache, and implements error and retry handling.
 */
public abstract class ActivityForAccessingTumOnline<T> extends ProgressActivity {

    protected TUMOnlineClient apiClient;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiClient = TUMOnlineClient.getInstance(this);
    }

    /**
     * Called when the user refreshes the screen via a pull-to-refresh gesture. Subclasses that
     * want to react to such gestures must override this method.
     */
    @Override
    public void onRefresh() {
        // Subclasses can override this method
    }

    /**
     * Fetches a call from TUMonline and uses the provided listener if the request was successful.
     *
     * @param call The {@link Call} to fetch
     */
    protected final void fetch(Call<T> call) {
        showLoadingStart();
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (BuildConfig.DEBUG) {
                    String origin = "";

                    if (response.raw().networkResponse() != null) {
                        origin += "network";
                    }

                    if (response.raw().cacheResponse() != null) {
                        origin += "cache";
                    }

                    if (origin.isEmpty()) {
                        origin = "none";
                    }

                    Utils.showToastOnUIThread(
                            ActivityForAccessingTumOnline.this, "Response from " + origin);
                }

                T body = response.body();
                if (response.isSuccessful() && body != null) {
                    onDownloadSuccessful(body);
                } else if (response.isSuccessful() && body == null) {
                    onEmptyDownloadResponse();
                } else {
                    onDownloadUnsuccessful(response.code());
                }
                showLoadingEnded();
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                onDownloadFailure(t);
            }
        });
    }

    protected abstract void onDownloadSuccessful(@NonNull T body);

    // TODO: Exponential Backoff for requests
    /*
    protected <T> void onRetry(TUMOnlineRequest<T> request, TUMOnlineResponseListener<T> listener) {
        fetchFromTumOnline(request, listener);
    }
    */

    /**
     * Called if the response from the API call is successful, but empty.
     */
    protected final void onEmptyDownloadResponse() {
        showError(R.string.error_empty_response);
    }

    /**
     * Called when a response is received, but that response is not successful. Displays the
     * appropriate error message, either in an error layout, or as a dialog or Snackbar.
     */
    protected final void onDownloadUnsuccessful(int statusCode) {
        if (statusCode == 503) {
            // The service is unavailable
            showError(R.string.error_tum_online_unavailable);
        } else {
            showError(R.string.error_unknown);
        }
    }

    /**
     * Called when an Exception is raised during an API call. Displays an error layout.
     * @param throwable The error that has occurred
     */
    protected final void onDownloadFailure(@NonNull Throwable throwable) {
        Utils.log(throwable);

        if (throwable instanceof NoNetworkConnectionException) {
            showNoInternetLayout();
        } else if (throwable instanceof InactiveTokenException) {
            showFailedTokenLayout(R.string.error_access_token_inactive);
        } else if (throwable instanceof InvalidTokenException) {
            showFailedTokenLayout(R.string.error_invalid_access_token);
        } else if (throwable instanceof MissingPermissionException) {
            showFailedTokenLayout(R.string.error_no_rights_to_access_function);
        } else if (throwable instanceof TokenLimitReachedException) {
            showFailedTokenLayout(R.string.error_access_token_limit_reached);
        } else if (throwable instanceof RequestLimitReachedException) {
            showError(R.string.error_request_limit_reached);
        } else {
            showError(R.string.error_unknown);
        }
    }

    // TODO: Further refactoring of the user interface
    // We should provide a more sophisticated display of errors. For instance, if there's already
    // content loaded into the view, do not show a full-screen progress bar. Instead, it should
    // be an overlay that preserves the data currently being displayed in the view.
    // A simple approach would be a Toast “Sending data…” and a second Toast “Sent data” once the
    // API call has successfully completed.

    /**
     * Displays a Snackbar with the specified message resource ID and a retry action. The retry
     * action once again enqueues the call with the provided request listener.
     * @param messageResId The resource ID of the message to be displayed in the Snackbar
     * @param call The Retrofit call that will be enqueued when retrying the request
     * @param listener The TUMOnlineRequestListener that will be called if the retry call
     *                 was successful
     * @param <T> The response type of the call
     */
    /*
    protected final <T> void displayApiErrorSnackbar(int messageResId, Call<T> call,
                                                     TUMOnlineResponseListener<T> listener) {
        // TODO: Use this at some point
        Snackbar snackbar = Snackbar.make(swipeRefreshLayout, messageResId, Snackbar.LENGTH_LONG);

        if (call != null && listener != null) {
            snackbar.setAction(R.string.retry, v -> onRetry(call, listener));
        }

        snackbar.show();
    }
    */

}
