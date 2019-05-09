package de.tum.`in`.tumcampusapp.component.other.generic.fragment

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ProgressActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Generic class which handles can handle a long running background task
 */
abstract class ActivityForLoadingInBackground<S, T>(
        layoutId: Int
) : ProgressActivity<T>(layoutId) {

    private var loadingDisposable: Disposable? = null
    private val isRunning = AtomicBoolean(false)
    private var lastArg: Array<out S>? = null

    /**
     * Called in separate thread after [.startLoading] gets called.
     * Should do all loading and return the result.
     *
     * @param arg Parameters given to [.startLoading]
     * @return Result of the loading task
     */
    protected abstract fun onLoadInBackground(vararg arg: S): T?

    /**
     * Gets called from the UI thread after background task has finished.
     *
     * @param result Result returned by [.onLoadInBackground]
     */
    protected abstract fun onLoadFinished(result: T?)

    /**
     * Starts a new background task.
     * The work that should be done in background must be specified in the [.onLoadInBackground] method.
     *
     * @param arg Arguments passed to [.onLoadInBackground]
     */
    @SafeVarargs
    protected fun startLoading(vararg arg: S) {
        // No concurrent background activity
        if (!isRunning.compareAndSet(false, true)) {
            return
        }

        lastArg = arg

        showLoadingStart()
        loadingDisposable = Observable.fromCallable<T> { onLoadInBackground(*arg) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    showLoadingEnded()
                    onLoadFinished(result)
                    isRunning.set(false)
                }, { t -> showError(R.string.error_something_wrong) })
    }

    override fun onRefresh() {
        lastArg?.let {
            startLoading(*it)
        }
    }

    protected override fun onDestroy() {
        super.onDestroy()
        if (loadingDisposable != null) {
            loadingDisposable!!.dispose()
        }
    }
}