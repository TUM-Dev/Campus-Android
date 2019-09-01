package de.tum.`in`.tumcampusapp.component.other.generic.fragment

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean

abstract class FragmentForLoadingInBackground<T>(
    @LayoutRes layoutId: Int,
    @StringRes titleResId: Int
) : BaseFragment<T>(layoutId, titleResId) {

    private var loadingDisposable = CompositeDisposable()
    private val isRunning = AtomicBoolean(false)

    /**
     * Starts a new background task. The work that should be done in background must be specified in
     * the [onLoadInBackground] method.
     *
     * @param arg Arguments passed to [onLoadInBackground]
     */
    @SafeVarargs
    protected fun startLoading() {
        // No concurrent background activity
        if (!isRunning.compareAndSet(false, true)) {
            return
        }

        showLoadingStart()
        loadingDisposable += Observable.fromCallable<T> { onLoadInBackground() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                showLoadingEnded()
                onLoadFinished(result)
                isRunning.set(false)
            }, { showError(R.string.error_something_wrong) })
    }

    /**
     * Called in separate thread after [startLoading] gets called.
     * Should do all loading and return the result.
     *
     * @param arg Parameters given to [startLoading]
     * @return Result of the loading task
     */
    protected abstract fun onLoadInBackground(): T?

    /**
     * Gets called from the UI thread after background task has finished.
     *
     * @param result Result returned by [onLoadInBackground]
     */
    protected abstract fun onLoadFinished(result: T?)

    override fun onRefresh() {
        startLoading()
    }

    override fun onDestroy() {
        loadingDisposable.dispose()
        super.onDestroy()
    }

}
