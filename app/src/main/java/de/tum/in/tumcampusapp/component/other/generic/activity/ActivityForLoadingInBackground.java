package de.tum.in.tumcampusapp.component.other.generic.activity;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.tum.in.tumcampusapp.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Generic class which handles can handle a long running background task
 */
public abstract class ActivityForLoadingInBackground<S, T> extends ProgressActivity<T> {

    private Disposable loadingDisposable;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private S[] lastArg;

    /**
     * Called in separate thread after {@link #startLoading(Object[])} gets called.
     * Should do all loading and return the result.
     *
     * @param arg Parameters given to {@link #startLoading(Object[])}
     * @return Result of the loading task
     */
    @SuppressWarnings("unchecked")
    protected abstract @Nullable
    T onLoadInBackground(S... arg);

    /**
     * Gets called from the UI thread after background task has finished.
     *
     * @param result Result returned by {@link #onLoadInBackground(Object[])}
     */
    protected abstract void onLoadFinished(@Nullable T result);

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
        // No concurrent background activity
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }

        lastArg = arg;

        showLoadingStart();
        loadingDisposable = Observable.fromCallable(() -> onLoadInBackground(arg))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    showLoadingEnded();
                    onLoadFinished(result);
                    isRunning.set(false);
                }, t -> showError(R.string.error_something_wrong));
    }

    @Override
    public void onRefresh() {
        startLoading(lastArg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDisposable != null) {
            loadingDisposable.dispose();
        }
    }
}
