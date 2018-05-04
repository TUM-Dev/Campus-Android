package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.arch.lifecycle.Lifecycle;
import android.support.v4.widget.SwipeRefreshLayout;

import com.google.common.base.Optional;
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle2.LifecycleProvider;

import javax.annotation.Nullable;

import de.tum.in.tumcampusapp.utils.NetUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Generic class which can handle a long running search in background.
 * Class parameter should be the class that holds the results of the background task.
 */
public abstract class ActivityForSearchingInBackground<T> extends ActivityForSearching {
    protected final LifecycleProvider<Lifecycle.Event> provider = AndroidLifecycle.createLifecycleProvider(this);

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
     * {@link ActivityForSearchingInBackground#onSearchFinished(Optional)}
     */
    protected abstract Optional<T> onSearchInBackground();

    /**
     * Gets called if a search query has been entered.
     * This method is always called from a thread that is not the UI thread, so long running
     * operations can be invoked directly in this method.
     * To bring the loaded results to the UI return the results and apply it in
     * {@link ActivityForSearchingInBackground#onSearchFinished(Optional)}
     *
     * @param query Query to search for
     * @return Loaded results
     */
    protected abstract Optional<T> onSearchInBackground(String query);

    /**
     * Gets called after background task has finished. The
     * background task's return value is passed to this method, but
     * this method is called from the UI thread so you can access UI elements from here.
     *
     * @param result Result from background task
     */
    protected abstract void onSearchFinished(Optional<T> result);

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

        Observable<Optional<T>> observable;
        if (query == null) {
            observable = Observable.fromCallable(this::onSearchInBackground);
        } else {
            observable = Observable.fromCallable(() -> onSearchInBackground(query));
        }

        observable.compose(provider.bindToLifecycle())
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(this::onSearchFinished);
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
        runOnUiThread(() -> ActivityForSearchingInBackground.super.showError(errorReason));
    }

    /**
     * Shows error layout.
     * Hides any progress indicator.
     */
    @Override
    protected void showErrorLayout() {
        runOnUiThread(ActivityForSearchingInBackground.super::showErrorLayout);
    }

    /**
     * Shows failed layout
     *
     * @param error Error Text to be toasted
     */
    @Override
    protected void showFailedTokenLayout(final String error) {
        runOnUiThread(() -> ActivityForSearchingInBackground.super.showFailedTokenLayout(error));
    }

    /**
     * Shows failed layout
     */
    @Override
    protected void showNoTokenLayout() {
        runOnUiThread(ActivityForSearchingInBackground.super::showNoTokenLayout);
    }

    /**
     * Shows failed layout
     */
    @Override
    protected void showNoInternetLayout() {
        runOnUiThread(ActivityForSearchingInBackground.super::showNoInternetLayout);
    }
}
