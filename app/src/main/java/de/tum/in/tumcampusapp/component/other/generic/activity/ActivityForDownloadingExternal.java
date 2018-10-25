package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.service.DownloadService;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Generic class which handles all basic tasks to download JSON or files from an
 * external source. It uses the DownloadService to download from external and
 * implements a rich user feedback with error progress and token related layouts.
 */
public abstract class ActivityForDownloadingExternal extends ProgressActivity {
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(DownloadService.BROADCAST_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    /**
     * Broadcast receiver getting notifications from the download service, if downloading was successful or not
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(DownloadService.BROADCAST_NAME)) {
                return;
            }

            String action = intent.getStringExtra(Const.ACTION_EXTRA);
            if (!action.isEmpty()) {
                Utils.logv("Broadcast received  <" + action + ">");
                if (action.equals(Const.COMPLETED)) {
                    showLoadingEnded();
                    // Calls onStart() to simulate a new start of the activity
                    // without downloading new data, since this receiver
                    // receives data from a new download
                    onStart();
                }

                if (action.equals(Const.ERROR)) {
                    int messageResId = intent.getIntExtra(Const.MESSAGE, 0);
                    showError(messageResId);
                }
            }
        }
    };

    @Override
    public void onRefresh() {
        requestDownload(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
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
        Intent service = new Intent(this, DownloadService.class);
        service.putExtra(Const.ACTION_EXTRA, method);
        service.putExtra(Const.FORCE_DOWNLOAD, forceDownload);
        service.putExtra("callback", new Bundle());
        startService(service);
    }

}
