package de.tum.in.tumcampusapp.activities.generic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.services.DownloadService;

public class ActivityForDownloadingExternal extends Activity {
	public final static int MENU_REFRESH = 0;
	private Activity activity = this;

	private RelativeLayout errorLayout;
	private int layoutId;
	private String method;
	private RelativeLayout progressLayout;

	public BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (!intent.getAction().equals(DownloadService.broadcast)) {
				return;
			}

			String message = intent.getStringExtra(Const.ACTION_EXTRA);
			if (message.length() != 0) {
				Log.i(activity.getClass().getSimpleName(), "Broadcast received  <" + message + ">");
				if (message.equals(Const.COMPLETED)) {
					progressLayout.setVisibility(View.GONE);
					errorLayout.setVisibility(View.GONE);
					onResume();
				}
				if (message.equals(Const.ERROR)) {
					progressLayout.setVisibility(View.GONE);
					errorLayout.setVisibility(View.VISIBLE);
				}
			}
		}
	};

	public ActivityForDownloadingExternal(String method, int layoutId) {
		this.method = method;
		this.layoutId = layoutId;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutId);

		progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.error_layout);

		if (progressLayout == null || errorLayout == null) {
			Log.e(getClass().getSimpleName(), "Cannot find layouts, did you forget to provide error and progress layouts?");
		}
		registerReceiver(receiver, new IntentFilter(DownloadService.broadcast));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem m = menu.add(0, MENU_REFRESH, 0, getString(R.string.update));
		m.setIcon(android.R.drawable.ic_menu_rotate);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		Intent service = new Intent(this, DownloadService.class);
		stopService(service);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			requestDownload();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void requestDownload() {
		if (Utils.isConnected(this)) {
			progressLayout.setVisibility(View.VISIBLE);
			Intent service = new Intent(this, DownloadService.class);
			service.putExtra(Const.ACTION_EXTRA, method);
			startService(service);
		} else {
			errorLayout.setVisibility(View.VISIBLE);
			Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
		}
	}

	// TODO: Make this nicer
	public void requestDownloadWithExtras(Bundle extras) {
		if (Utils.isConnected(this)) {
			progressLayout.setVisibility(View.VISIBLE);
			Intent service = new Intent(this, DownloadService.class);
			service.putExtra(Const.ACTION_EXTRA, method);
			service.putExtras(extras);
			startService(service);
		} else {
			errorLayout.setVisibility(View.VISIBLE);
			Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
		}
	}

}
