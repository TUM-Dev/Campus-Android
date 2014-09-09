package de.tum.in.tumcampus.activities.generic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.services.DownloadService;

/**
 * Generic class which handles all basic tasks to download JSON or files from an
 * external source. It uses the DownloadService to download from external and
 * implements a rich user feedback with error progress and token related
 * layouts.
 * 
 */
public abstract class ActivityForDownloadingExternal extends ActionBarActivity {
	private String method;

    /** Default layouts for user interaction */
    private int mLayoutId;
    protected RelativeLayout errorLayout;
    protected RelativeLayout progressLayout;

    public ActivityForDownloadingExternal(String method, int layoutId) {
        this.mLayoutId = layoutId;
        this.method = method;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
        setContentView(mLayoutId);

        progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
        errorLayout = (RelativeLayout) findViewById(R.id.error_layout);

        if (progressLayout == null || errorLayout == null) {
            Log.e(getClass().getSimpleName(), "Cannot find layouts, did you forget to provide error and progress layouts?");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update:
                requestDownload(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	public BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (!intent.getAction().equals(DownloadService.BROADCAST_NAME)) {
				return;
			}

			String action = intent.getStringExtra(Const.ACTION_EXTRA);
			if (action.length() != 0) {
				Log.i(ActivityForDownloadingExternal.this.getClass().getSimpleName(), "Broadcast received  <" + action + ">");
				if (action.equals(Const.COMPLETED)) {
					progressLayout.setVisibility(View.GONE);
					errorLayout.setVisibility(View.GONE);
					// Calls onStart() to simulate a new start of the activity
					// without downloading new data, since this receiver
					// receives data from a new download
					onStart();
				}
				if (action.equals(Const.WARNING)) {
					String message = intent.getStringExtra(Const.WARNING_MESSAGE);
					Toast.makeText(ActivityForDownloadingExternal.this, message, Toast.LENGTH_SHORT).show();
					progressLayout.setVisibility(View.GONE);
				}
				if (action.equals(Const.ERROR)) {
					String message = intent.getStringExtra(Const.ERROR_MESSAGE);
					Toast.makeText(ActivityForDownloadingExternal.this, message, Toast.LENGTH_SHORT).show();
					progressLayout.setVisibility(View.GONE);
					errorLayout.setVisibility(View.VISIBLE);
				}
			}
		}
	};

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(DownloadService.BROADCAST_NAME));
    }

	@Override
	protected void onPause() {
		super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		//stopService(new Intent(this, DownloadService.class));
	}

	public void requestDownload(boolean forceDownload) {
		if (Utils.isConnected(this)) {
			errorLayout.setVisibility(View.GONE);
			progressLayout.setVisibility(View.VISIBLE);
			Intent service = new Intent(this, DownloadService.class);
			service.putExtra(Const.ACTION_EXTRA, method);
			service.putExtra(Const.FORCE_DOWNLOAD, forceDownload);
			startService(service);
		} else {
			Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
		}
	}

	public void requestDownloadWithExtras(Bundle extras, boolean forceDownload) {
		if (Utils.isConnected(this)) {
			progressLayout.setVisibility(View.VISIBLE);
			Intent service = new Intent(this, DownloadService.class);
			service.putExtra(Const.ACTION_EXTRA, method);
			service.putExtras(extras);
			startService(service);
		} else {
			Toast.makeText(this, R.string.no_internet_connection,
					Toast.LENGTH_SHORT).show();
		}
	}

    public void showError(int errorReason) {
        showError(getString(errorReason));
    }

    public void showError(String errorReason) {
        Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();
        progressLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    public void showErrorLayout() {
        errorLayout.setVisibility(View.VISIBLE);
    }

    public void showProgressLayout() {
        progressLayout.setVisibility(View.VISIBLE);
    }

    public void hideErrorLayout() {
        errorLayout.setVisibility(View.GONE);
    }

    public void hideProgressLayout() {
        progressLayout.setVisibility(View.GONE);
    }
}
