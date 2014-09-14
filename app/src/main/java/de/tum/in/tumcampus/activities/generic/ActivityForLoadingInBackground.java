package de.tum.in.tumcampus.activities.generic;

import android.os.AsyncTask;
import android.view.View;

import de.tum.in.tumcampus.R;

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
    protected T1[] lastArg;

    public void startLoading(final T1... arg) {
        if(asyncTask!=null)
            asyncTask.cancel(true);

        lastArg = arg;

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
