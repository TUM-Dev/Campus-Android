package de.tum.`in`.tumcampusapp.component.other.generic.activity

import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl.BYPASS_CACHE
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

/**
 * Generic class which handles all basic tasks to download JSON or files from an external source.
 * It implements a rich user feedback with error progress and token related layouts.
 * The given layout must include a progress_layout, failed_layout, no_token_layout and an
 * error_layout.
 * If the Activity should support Pull-To-Refresh it can also contain a  [SwipeRefreshLayout] named
 * ptr_layout
 *
 * @param layoutId Resource id of the xml layout that should be used to inflate the activity
 */
@Deprecated("Use BaseActivity and a suitable BaseFragment class")
abstract class ActivityForDownloadingExternal(layoutId: Int) : ProgressActivity<Void>(layoutId) {

    /**
     * The [DownloadWorker.Action] to be executed
     */
    abstract val method: DownloadWorker.Action?

    /**
     * Gets notifications from the DownloadWorker, if downloading was successful or not
     */
    private val completionHandler = Observer<Unit> {
        // Calls onStart() to simulate a new start of the activity
        // without downloading new data, since this receiver
        // receives data from a new download
        onStart()
    }

    private val errorHandler = {
        showError(R.string.something_wrong)
    }

    override fun onRefresh() {
        requestDownload(BYPASS_CACHE)
    }

    /**
     * Start a download of the specified type
     *
     * @param forceDownload If we should throw away cached data and re-download instead.
     */
    protected fun requestDownload(forceDownload: CacheControl) {
        if (!NetUtils.isConnected(this)) {
            Utils.showToast(this, R.string.no_internet_connection)
        }

        showLoadingStart()
        LiveDataReactiveStreams
                .fromPublisher<Unit>(
                        Flowable.fromCallable { method?.execute(forceDownload) }
                                .doOnError { errorHandler() }
                                .onErrorReturnItem(Unit)
                                .subscribeOn(Schedulers.io())
                )
                .observe(this, completionHandler)
    }

}
