package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
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
public abstract class ActivityForSearchingTumOnline<T> extends ActivityForSearching {

    protected final TUMOnlineClient apiClient;

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
        apiClient = TUMOnlineClient.getInstance(this);
    }

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
     * @param statusCode The HTTP status code of the response
     */
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
        showErrorLayout(throwable);
    }

}
