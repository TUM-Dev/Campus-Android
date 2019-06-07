package de.tum.`in`.tumcampusapp.component.other.generic.fragment

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

abstract class FragmentForSearchingInBackground<T>(
    @LayoutRes layoutId: Int,
    @StringRes titleResId: Int,
    auth: String,
    minLen: Int
) : FragmentForSearching<T>(layoutId, titleResId, auth, minLen) {

    private var compositeDisposable = CompositeDisposable()

    /**
     * Gets called if search has been canceled.
     * This method is always called from a thread that is not the UI thread, so long running
     * operations can be invoked directly in this method.
     * To bring the loaded results to the UI return the results and apply it in
     * [ActivityForSearchingInBackground.onSearchFinished]
     */
    protected abstract fun onSearchInBackground(): T?

    /**
     * Gets called if a search query has been entered.
     * This method is always called from a thread that is not the UI thread, so long running
     * operations can be invoked directly in this method.
     * To bring the loaded results to the UI return the results and apply it in
     * [ActivityForSearchingInBackground.onSearchFinished]
     *
     * @param query Query to search for
     * @return Loaded results
     */
    protected abstract fun onSearchInBackground(query: String): T?

    /**
     * Gets called after background task has finished. The
     * background task's return value is passed to this method, but
     * this method is called from the UI thread so you can access UI elements from here.
     *
     * @param result Result from background task
     */
    protected abstract fun onSearchFinished(result: T?)

    override fun onStartSearch() {
        onStartSearch(null)
    }

    override fun onStartSearch(query: String?) {
        if (!NetUtils.isConnected(requireContext())) {
            showNoInternetLayout()
            return
        }

        showLoadingStart()

        val observable = if (query == null) {
            Observable.fromCallable { onSearchInBackground() }
        } else {
            Observable.fromCallable { onSearchInBackground(query) }
        }

        compositeDisposable += observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSearchFinished) {
                    showError(R.string.error_something_wrong)
                }
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        super.onDestroyView()
    }
}
