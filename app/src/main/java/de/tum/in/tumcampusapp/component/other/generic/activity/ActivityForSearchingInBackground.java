package de.tum.in.tumcampusapp.component.other.generic.activity;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.utils.NetUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Generic class which can handle a long running search in background.
 * Class parameter should be the class that holds the results of the background task.
 */
@Deprecated
public abstract class ActivityForSearchingInBackground<T> extends ActivityForSearching<T> {

    private Disposable searchDisposable;

    /**
     * Initializes an activity for searching in background.
     * The xml layout must include an error_layout and a progress_layout.
     * A {@link SwipeRefreshLayout}
     * called ptr_layout is required if the activity should support PullToRefresh method
     *
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     * @param auth     Authority for search suggestions declared in manifest file
     * @param minLen   Minimum text length that has to be entered by the user before a search quest can be submitted
     */
    public ActivityForSearchingInBackground(int layoutId, String auth, int minLen) {
        super(layoutId, auth, minLen);
    }

    /**
     * Gets called if search has been canceled.
     * This method is always called from a thread that is not the UI thread, so long running
     * operations can be invoked directly in this method.
     * To bring the loaded results to the UI return the results and apply it in
     * {@link ActivityForSearchingInBackground#onSearchFinished(T)}
     */
    protected abstract @Nullable
    T onSearchInBackground();

    /**
     * Gets called if a search query has been entered.
     * This method is always called from a thread that is not the UI thread, so long running
     * operations can be invoked directly in this method.
     * To bring the loaded results to the UI return the results and apply it in
     * {@link ActivityForSearchingInBackground#onSearchFinished(T)}
     *
     * @param query Query to search for
     * @return Loaded results
     */
    protected abstract @Nullable T onSearchInBackground(String query);

    /**
     * Gets called after background task has finished. The
     * background task's return value is passed to this method, but
     * this method is called from the UI thread so you can access UI elements from here.
     *
     * @param result Result from background task
     */
    protected abstract void onSearchFinished(@Nullable T result);

    @Override
    public final void onStartSearch() {
        onStartSearch(null);
    }

    @Override
    public final void onStartSearch(@Nullable String query) {
        if (!NetUtils.isConnected(ActivityForSearchingInBackground.this)) {
            showNoInternetLayout();
            return;
        }

        showLoadingStart();

        Observable<T> observable;
        if (query == null) {
            observable = Observable.fromCallable(this::onSearchInBackground);
        } else {
            observable = Observable.fromCallable(() -> onSearchInBackground(query));
        }

        searchDisposable = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSearchFinished, t -> showError(R.string.error_something_wrong));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
    }
}
