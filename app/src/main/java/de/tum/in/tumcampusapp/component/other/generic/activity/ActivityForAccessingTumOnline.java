package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
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
    private boolean hadSuccessfulRequest;

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
                T body = response.body();
                if (response.isSuccessful() && body != null) {
                    hadSuccessfulRequest = true;
                    onDownloadSuccessful(body);
                } else if (response.isSuccessful()) {
                    onEmptyDownloadResponse();
                } else {
                    onDownloadUnsuccessful(response.code());
                }
                showLoadingEnded();
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                showLoadingEnded();
                onDownloadFailure(t);
            }
        });
    }

    protected abstract void onDownloadSuccessful(@NonNull T body);

    /**
     * Called if the response from the API call is successful, but empty.
     */
    protected final void onEmptyDownloadResponse() {
        showError(R.string.error_no_data_to_show);
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
            showErrorSnackbar(R.string.error_unknown);
        }
    }

    /**
     * Called when an Exception is raised during an API call. Displays an error layout.
     * @param throwable The error that has occurred
     */
    protected final void onDownloadFailure(@NonNull Throwable throwable) {
        Utils.log(throwable);

        if (hadSuccessfulRequest) {
            showErrorSnackbar(throwable);
        } else {
            showErrorLayout(throwable);
        }
    }

}
