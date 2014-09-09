package de.tum.in.tumcampus.activities.generic;

import android.os.AsyncTask;
import android.view.View;

/**
 * Generic class which handles can handle a long running background task
 */
public abstract class ActivityForLoadingInBackground<T1,T2> extends ProgressActivity {

    public ActivityForLoadingInBackground(int layoutId) {
		super(layoutId);
	}

    protected abstract T2 onLoadInBackground(T1... arg);
    protected abstract void onLoadFinished(T2 result);

    protected AsyncTask<T1, Void, T2> asyncTask;

    public void startLoading(final T1... arg) {
        if(asyncTask!=null)
            asyncTask.cancel(true);

        asyncTask = new AsyncTask<T1,Void,T2>() {
            @Override
            protected void onPreExecute() {
                progressLayout.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
            }

            @Override
            protected T2 doInBackground(T1... arg) {
                return onLoadInBackground(arg);
            }

            @Override
            protected void onPostExecute(T2 result) {
                progressLayout.setVisibility(View.GONE);
                onLoadFinished(result);
                asyncTask = null;
            }
        };
        asyncTask.execute(arg);
	}

    protected void onCancelLoading() {
        if (asyncTask!=null) {
            asyncTask.cancel(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onCancelLoading();
    }
}
