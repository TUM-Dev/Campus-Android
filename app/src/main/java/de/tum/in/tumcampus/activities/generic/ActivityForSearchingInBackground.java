package de.tum.in.tumcampus.activities.generic;

import android.os.AsyncTask;
import android.view.View;

/**
 * Generic class which handles can handle a long running background searches
 */
public abstract class ActivityForSearchingInBackground<T> extends ActivityForSearching {

    public ActivityForSearchingInBackground(int layoutId, String auth, int len) {
		super(layoutId, auth, len);
	}

    protected abstract T onSearchInBackground();
    protected abstract T onSearchInBackground(String arg);
    protected abstract void onSearchFinished(T result);

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

    private class BackgroundSearch extends AsyncTask<String,Void,T> {
        @Override
        protected T doInBackground(String... arg) {
            if(arg.length==0)
                return ActivityForSearchingInBackground.this.onSearchInBackground();
            return ActivityForSearchingInBackground.this.onSearchInBackground(arg[0]);
        }

        @Override
        protected void onPostExecute(T result) {
            progressLayout.setVisibility(View.GONE);
            onSearchFinished(result);
            asyncTask = null;
        }
    }
}
