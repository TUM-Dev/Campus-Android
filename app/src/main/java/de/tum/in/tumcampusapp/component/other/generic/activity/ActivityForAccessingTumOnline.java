package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.exception.NoNetworkConnectionException;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineAPIService;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
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
 * Generic class which handles all basic tasks to communicate with TUMOnline. It
 * implements a rich user feedback with error progress and token-related layouts.
 */
public abstract class ActivityForAccessingTumOnline extends ProgressActivity {

    protected final TUMOnlineAPIService mApiService = TUMOnlineClient.getInstance(this);

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
    public void onRefresh() {
        // Subclasses can override this method
    }

    protected <T> void onRetry(Call<T> call, TUMOnlineResponseListener<T> listener) {
        fetch(call, listener);
    }

    // TODO: Further refactoring of the user interface
    // We should provide a more sophisticated display of errors. For instance, if there's already
    // content loaded into the view, do not show a full-screen progress bar. Instead, it should
    // be an overlay that preserves the data currently being displayed in the view.
    // A simple approach would be a Toast “Sending data…” and a second Toast “Sent data” once the
    // API call has successfully completed.

    /**
     * Fetches a call and uses the provided listener if the request was successful.
     * @param call The API call to perform
     * @param listener The response listener that is invoked if the request was successful
     * @param <T> The response type of the call
     */
    protected <T> void fetch(Call<T> call, TUMOnlineResponseListener<T> listener) {
        showLoadingStart();
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                T body = response.body();
                if (response.isSuccessful() && body != null) {
                    listener.onDownloadSuccessful(body);
                } else if (body == null) {
                    onEmptyDownloadResponse(call, listener);
                } else {
                    onDownloadUnsuccessful(response.code(), call, listener);
                }
                showLoadingEnded();
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                showLoadingEnded();
                onDownloadFailure(call, listener, t);
            }
        });
    }

    protected final <T> void onEmptyDownloadResponse(Call<T> call,
                                                     TUMOnlineResponseListener<T> listener) {
        showError(R.string.error_empty_response, call, listener);
    }

    /**
     * Called when a response is received, but that response is not successful. Displays the
     * appropriate error message, either in an error layout, or as a dialog or Snackbar.
     * @param statusCode The HTTP status code of the response
     */
    protected final <T> void onDownloadUnsuccessful(int statusCode, Call<T> call,
                                                    TUMOnlineResponseListener<T> listener) {
        if (statusCode == 503) {
            // The service is unavailable
            showError(R.string.error_tum_online_unavailable, call, listener);
        } else {
            showError(R.string.error_unknown, call, listener);
        }
    }

    protected final <T> void showError(int messageResId, Call<T> call,
                                       TUMOnlineResponseListener<T> listener) {
        super.showError(messageResId);
        errorLayout.setOnClickListener(v -> fetch(call, listener));
    }

    /**
     * Called when an Exception is raised during an API call. Displays either an error layout if the
     * failing request was the first request, or shows an error of Snackbar if it's a subsequent
     * request.
     * @param call The call that has failed
     * @param listener The TUMOnlineRequestListener that will be called if the retry call
     *                 was successful
     * @param throwable The error that has occurred
     * @param <T> The response type of the call
     */
    protected final <T> void onDownloadFailure(@NonNull Call<T> call,
                                               TUMOnlineResponseListener<T> listener,
                                               @NonNull Throwable throwable) {
        Utils.log(throwable);
        showLoadingEnded();

        if (throwable instanceof NoNetworkConnectionException) {
            showNoInternetLayout(call, listener);
        } else if (throwable instanceof InactiveTokenException) {
            showFailedTokenLayout(R.string.error_access_token_inactive, call, listener);
        } else if (throwable instanceof InvalidTokenException) {
            showFailedTokenLayout(R.string.error_invalid_access_token, call, listener);
        } else if (throwable instanceof MissingPermissionException) {
            showFailedTokenLayout(R.string.error_no_rights_to_access_function, call, listener);
        } else if (throwable instanceof TokenLimitReachedException) {
            showFailedTokenLayout(R.string.error_access_token_limit_reached, call, listener);
        } else if (throwable instanceof RequestLimitReachedException) {
            showError(R.string.error_request_limit_reached, call, listener);
        } else if (throwable instanceof UnknownErrorException) {
            showError(R.string.error_unknown, call, listener);
        }
    }

    protected final <T> void showNoInternetLayout(Call<T> call,
                                                  TUMOnlineResponseListener<T> listener) {
        super.showNoInternetLayout();
        noInternetLayout.setOnClickListener(v -> fetch(call, listener));
    }

    protected final <T> void showFailedTokenLayout(int messageResId, Call<T> call,
                                                   TUMOnlineResponseListener<T> listener) {
        super.showFailedTokenLayout(messageResId);
        failedTokenLayout.setOnClickListener(v -> fetch(call, listener));
    }

    /**
     * Displays an error dialog with the provided message resource ID.
     * @param messageResId The resource ID of the message to be displayed in the dialog
     */
    protected final void displayApiErrorDialog(int messageResId) {
        // TODO: Use this at some point
        new AlertDialog.Builder(this)
                .setMessage(messageResId)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(false)
                .show();
    }

    /**
     * Displays a Snackbar with the specified message resource ID and a retry action. The retry
     * action once again enqueues the call with the provided request listener.
     * @param messageResId The resource ID of the message to be displayed in the Snackbar
     * @param call The Retrofit call that will be enqueued when retrying the request
     * @param listener The TUMOnlineRequestListener that will be called if the retry call
     *                 was successful
     * @param <T> The response type of the call
     */
    protected final <T> void displayApiErrorSnackbar(int messageResId, Call<T> call,
                                                     TUMOnlineResponseListener<T> listener) {
        // TODO: Use this at some point
        Snackbar snackbar = Snackbar.make(swipeRefreshLayout, messageResId, Snackbar.LENGTH_LONG);

        if (call != null && listener != null) {
            snackbar.setAction(R.string.retry, v -> onRetry(call, listener));
        }

        snackbar.show();
    }

}
