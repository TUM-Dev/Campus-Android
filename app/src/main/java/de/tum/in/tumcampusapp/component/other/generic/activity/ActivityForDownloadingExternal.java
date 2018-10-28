package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.arch.lifecycle.Observer;
import android.support.v4.widget.SwipeRefreshLayout;

import androidx.work.State;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.WorkStatus;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.service.DownloadWorker;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Generic class which handles all basic tasks to download JSON or files from an
 * external source. It uses the DownloadService to download from external and
 * implements a rich user feedback with error progress and token related layouts.
 */
public abstract class ActivityForDownloadingExternal extends ProgressActivity {
    protected final WorkManager workManager;
    private final String method;

    /**
     * Standard constructor for ActivityForAccessingTumOnline.
     * The given layout must include a progress_layout, failed_layout, no_token_layout and an error_layout.
     * If the Activity should support Pull-To-Refresh it can also contain a
     * {@link SwipeRefreshLayout} named ptr_layout
     *
     * @param method   Type of content to be downloaded
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     */
    public ActivityForDownloadingExternal(String method, int layoutId) {
        super(layoutId);
        this.method = method;
        workManager = WorkManager.getInstance();
    }

    /**
     * Gets notifications from the DownloadWorker, if downloading was successful or not
     */
    private final Observer<WorkStatus> workStatusObserver = workStatus -> {
        if (workStatus == null || !workStatus.getState().isFinished()) {
            return;
        }
        if (workStatus.getState() == State.SUCCEEDED) {
            showLoadingEnded();
            // Calls onStart() to simulate a new start of the activity
            // without downloading new data, since this receiver
            // receives data from a new download
            onStart();
            return;
        }
        // Finished state without success -> some kind of error
        showError(R.string.something_wrong);
    };

    @Override
    public void onRefresh() {
        requestDownload(true);
    }

    /**
     * Start a download of the specified type
     *
     * @param forceDownload If set to true if will force the download service
     *                      to throw away cached data and re-download instead.
     */
    protected void requestDownload(boolean forceDownload) {
        if (!NetUtils.isConnected(this)) {
            Utils.showToast(this, R.string.no_internet_connection);
        }

        showLoadingStart();

        WorkRequest workRequest = DownloadWorker.getWorkRequest(method, forceDownload);
        workManager.enqueue(workRequest);
        workManager.getStatusByIdLiveData(workRequest.getId())
                .observe(this, workStatusObserver);
    }

}
