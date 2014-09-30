package de.tum.in.tumcampus.activities.generic;

import android.os.AsyncTask;

/**
 * Generic class which can handle a long running search in background.
 * Class parameter should be the class that holds the results of the background task.
 */
public abstract class ActivityForSearchingInBackground<T> extends ActivityForSearching {
    /**
     * Gets called if search has been canceled.
     * This method is always called from a thread that is not the UI thread, so long running
     * operations can be invoked directly in this method.
     * To bring the loaded results to the UI return the results and apply it in
     * {@link de.tum.in.tumcampus.activities.generic.ActivityForSearchingInBackground#onSearchFinished(Object)}
     */
    protected abstract T onSearchInBackground();

    /**
     * Gets called if a search query has been entered.
     * This method is always called from a thread that is not the UI thread, so long running
     * operations can be invoked directly in this method.
     * To bring the loaded results to the UI return the results and apply it in
     * {@link de.tum.in.tumcampus.activities.generic.ActivityForSearchingInBackground#onSearchFinished(Object)}
     * @param query Query to search for
     * @return Loaded results
     */
    protected abstract T onSearchInBackground(String query);

    /**
     * Gets called after background task has finished. The
     * background task's return value is passed to this method, but
     * this method is called from the UI thread so you can access UI elements from here.
     * @param result Result from background task
     * */
    protected abstract void onSearchFinished(T result);

    /**
     * Initializes an activity for searching in background.
     * The xml layout must include an error_layout and a progress_layout.
     * A {@link uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout}
     * called ptr_layout is required if the activity should support PullToRefresh method
     *
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     * @param auth Authority for search suggestions declared in manifest file
     * @param minLen Minimum text length that has to be entered by the user before a search quest can be submitted
     */
    public ActivityForSearchingInBackground(int layoutId, String auth, int minLen) {
		super(layoutId, auth, minLen);
	}

    protected AsyncTask<String, Void, T> asyncTask;

    @Override
    public final void onStartSearch() {
        if(asyncTask!=null)
            asyncTask.cancel(true);

        asyncTask = new BackgroundSearch();
        asyncTask.execute();
    }

    @Override
    public final void onStartSearch(final String query) {
        if(asyncTask!=null)
            asyncTask.cancel(true);

        asyncTask = new BackgroundSearch();
        asyncTask.execute(query);
	}

    void onCancelLoading() {
        if (asyncTask!=null) {
            asyncTask.cancel(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onCancelLoading();
    }

    private class BackgroundSearch extends AsyncTask<String,Void,T> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingStart();
        }

        @Override
        protected T doInBackground(String... arg) {
            if(arg.length==0)
                return onSearchInBackground();
            return onSearchInBackground(arg[0]);
        }

        @Override
        protected void onPostExecute(T result) {
            showLoadingEnded();
            onSearchFinished(result);
            asyncTask = null;
        }
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
    protected void showError(final String errorReason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ActivityForSearchingInBackground.super.showError(errorReason);
            }
        });
    }

    /**
     * Shows error layout.
     * Hides any progress indicator.
     */
    @Override
    protected void showErrorLayout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ActivityForSearchingInBackground.super.showErrorLayout();
            }
        });
    }

    /**
     * Shows failed layout
     * @param error Error Text to be toasted
     */
    @Override
    protected void showFailedTokenLayout(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ActivityForSearchingInBackground.super.showFailedTokenLayout(error);
            }
        });
    }

    /**
     * Shows failed layout
     */
    @Override
    protected void showNoTokenLayout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ActivityForSearchingInBackground.super.showNoTokenLayout();
            }
        });
    }

    /**
     * Shows failed layout
     */
    @Override
    protected void showNoInternetLayout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ActivityForSearchingInBackground.super.showNoInternetLayout();
            }
        });
    }
}
