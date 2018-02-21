package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Generic class which handles can handle a long running background task
 */
public abstract class ActivityForLoadingInBackground<S, T> extends ProgressActivity {

    private AsyncTask<S, Void, T> asyncTask;
    private S[] lastArg;

    /**
     * Called in separate thread after {@link #startLoading(Object[])} gets called.
     * Should do all loading and return the result.
     *
     * @param arg Parameters given to {@link #startLoading(Object[])}
     * @return Result of the loading task
     */
    @SuppressWarnings("unchecked")
    protected abstract T onLoadInBackground(S... arg);

    /**
     * Gets called from the UI thread after background task has finished.
     *
     * @param result Result returned by {@link #onLoadInBackground(Object[])}
     */
    protected abstract void onLoadFinished(T result);

    /**
     * Standard constructor for ActivityForLoadingInBackground.
     * The given layout must include a progress_layout and an error_layout.
     * If the Activity should support Pull-To-Refresh it can also contain a
     * {@link SwipeRefreshLayout} named ptr_layout
     *
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     */
    public ActivityForLoadingInBackground(int layoutId) {
        super(layoutId);
    }

    /**
     * Starts a new background task.
     * The work that should be done in background must be specified in the {@link #onLoadInBackground(Object[])} method.
     *
     * @param arg Arguments passed to {@link #onLoadInBackground(Object[])}
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    protected final void startLoading(final S... arg) {
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }

        lastArg = arg;

        asyncTask = new AsyncTask<S, Void, T>() {
            @Override
            protected void onPreExecute() {
                showLoadingStart();
            }

            @SafeVarargs
            @Override
            protected final T doInBackground(S... arg) {
                return onLoadInBackground(arg);
            }

            @Override
            protected void onPostExecute(T result) {
                showLoadingEnded();
                onLoadFinished(result);
                asyncTask = null;
            }
        };
        asyncTask.execute(arg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
    }

    @Override
    public void onRefresh() {
        startLoading(lastArg);
    }

    /**
     * Shows error layout and toasts the given message.
     * Hides any progress indicator.
     *
     * @param errorReason Resource id of the error text
     */
    @Override
    protected void showError(int errorReason) {
        showError(getString(errorReason));
    }

    /**
     * Shows error layout and toasts the given message.
     * Hides any progress indicator.
     *
     * @param errorReason Error text
     */
    @Override
    protected void showError(final String errorReason) {
        runOnUiThread(() -> ActivityForLoadingInBackground.super.showError(errorReason));
    }

    /**
     * Shows error layout.
     * Hides any progress indicator.
     */
    @Override
    protected void showErrorLayout() {
        runOnUiThread(ActivityForLoadingInBackground.super::showErrorLayout);
    }

    /**
     * Shows failed layout
     *
     * @param error Error Text to be toasted
     */
    @Override
    protected void showFailedTokenLayout(final String error) {
        runOnUiThread(() -> ActivityForLoadingInBackground.super.showFailedTokenLayout(error));
    }

    /**
     * Shows failed layout
     */
    @Override
    protected void showNoTokenLayout() {
        runOnUiThread(ActivityForLoadingInBackground.super::showNoTokenLayout);
    }

    /**
     * Shows failed layout
     */
    @Override
    protected void showNoInternetLayout() {
        runOnUiThread(ActivityForLoadingInBackground.super::showNoInternetLayout);
    }
}
