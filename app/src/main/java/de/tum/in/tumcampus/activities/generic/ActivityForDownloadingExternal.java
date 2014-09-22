package de.tum.in.tumcampus.activities.generic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.services.DownloadService;

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
     * {@link uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout} named ptr_layout
     *
     * @param method Type of content to be downloaded
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     */
    //TODO replace method with enum
    public ActivityForDownloadingExternal(String method, int layoutId) {
        super(layoutId);
        this.method = method;
    }

    /** Broadcast receiver getting notifications from the download service, if downloading was successful or not */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (!intent.getAction().equals(DownloadService.BROADCAST_NAME)) {
				return;
			}

			String action = intent.getStringExtra(Const.ACTION_EXTRA);
			if (action.length() != 0) {
				Log.i(ActivityForDownloadingExternal.this.getClass().getSimpleName(), "Broadcast received  <" + action + ">");
				if (action.equals(Const.COMPLETED)) {
					showLoadingEnded();
					// Calls onStart() to simulate a new start of the activity
					// without downloading new data, since this receiver
					// receives data from a new download
					onStart();
				}
				if (action.equals(Const.WARNING)) {
					String message = intent.getStringExtra(Const.WARNING_MESSAGE);
					Utils.showToast(ActivityForDownloadingExternal.this, message);
                    showLoadingEnded();
				}
				if (action.equals(Const.ERROR)) {
					String message = intent.getStringExtra(Const.ERROR_MESSAGE);
					showError(message);
				}
			}
		}
	};

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.error_layout:
                requestDownload(true);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(DownloadService.BROADCAST_NAME));
    }

	@Override
	protected void onPause() {
		super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}

    /**
     * Start a download of the specified type
     * @param forceDownload If set to true if will force the download service
     *                      to throw away cached data and re-download instead.
     */
    protected void requestDownload(boolean forceDownload) {
		if (Utils.isConnected(this)) {
            showLoadingStart();
			Intent service = new Intent(this, DownloadService.class);
			service.putExtra(Const.ACTION_EXTRA, method);
			service.putExtra(Const.FORCE_DOWNLOAD, forceDownload);
			startService(service);
		} else {
			showError(R.string.no_internet_connection); // TODO Make generic layout for no internet
		}
	}
}
