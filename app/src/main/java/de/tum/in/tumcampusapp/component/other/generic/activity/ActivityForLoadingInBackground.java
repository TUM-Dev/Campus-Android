package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.arch.lifecycle.Lifecycle;
import android.support.v4.widget.SwipeRefreshLayout;

import com.google.common.base.Optional;
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle2.LifecycleProvider;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Generic class which handles can handle a long running background task
 */
public abstract class ActivityForLoadingInBackground<S, T> extends ProgressActivity {

    private final LifecycleProvider<Lifecycle.Event> provider = AndroidLifecycle.createLifecycleProvider(this);
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
        // No concurrent background activity
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }

        lastArg = arg;

        showLoadingStart();
        Observable.fromCallable(() -> Optional.fromNullable(onLoadInBackground(arg)))
                .compose(provider.bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturnItem(Optional.absent())
                .subscribe((result) -> {
                    showLoadingEnded();
                    onLoadFinished(result.orNull());
                    isRunning.set(false);
                });
    }

    @Override
    public void onRefresh() {
        startLoading(lastArg);
    }

}
