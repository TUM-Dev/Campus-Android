package de.tum.in.tumcampus.activities.generic;

import android.os.AsyncTask;
import android.view.View;

import de.tum.in.tumcampus.R;

/**
 * Generic class which handles can handle a long running background task
 */
public abstract class ActivityForLoadingInBackground<T1,T2> extends ProgressActivity {

    /**
     * Called in separate thread after {@link #startLoading(Object[])} gets called.
     * Should do all loading and return the result.
     * @param arg Parameters given to {@link #startLoading(Object[])}
     * @return Result of the loading task
     */
    protected abstract T2 onLoadInBackground(T1... arg);

    /**
     * Gets called from the UI thread after background task has finished.
     * @param result Result returned by {@link #onLoadInBackground(Object[])}
     */
    protected abstract void onLoadFinished(T2 result);

    /**
     * Standard constructor for ActivityForLoadingInBackground.
     * The given layout must include a progress_layout and an error_layout.
     * If the Activity should support Pull-To-Refresh it can also contain a
     * {@link uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout} named ptr_layout
     *
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     */
    public ActivityForLoadingInBackground(int layoutId) {
		super(layoutId);
	}

    private AsyncTask<T1, Void, T2> asyncTask;
    private T1[] lastArg;

    /**
     * Starts a new background task.
     * The work that should be done in background must be specified in the {@link #onLoadInBackground(Object[])} method.
     * @param arg Arguments passed to {@link #onLoadInBackground(Object[])}
     */
    protected void startLoading(final T1... arg) {
        if(asyncTask!=null)
            asyncTask.cancel(true);

        lastArg = arg;

        asyncTask = new AsyncTask<T1,Void,T2>() {
            @Override
            protected void onPreExecute() {
                showLoadingStart();
            }

            @Override
            protected T2 doInBackground(T1... arg) {
                return onLoadInBackground(arg);
            }

            @Override
            protected void onPostExecute(T2 result) {
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
        if (asyncTask!=null) {
            asyncTask.cancel(true);
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.error_layout:
                if(lastArg.length==0)
                    startLoading();
                else if(lastArg.length==1)
                    startLoading(lastArg[0]);
                else
                    startLoading(lastArg[0],lastArg[1]);
                break;
        }
    }
}
