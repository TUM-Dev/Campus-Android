package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.GalleryManager;
import de.tum.in.tumcampusapp.services.DownloadService;

/**
 * Activity to show gallery items (name, image, etc.)
 */
public class GalleryActivity extends Activity implements OnItemClickListener {
	public final static int MENU_REFRESH = 0;
	Activity activity = this;
	RelativeLayout errorLayout;
	RelativeLayout progressLayout;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		progressLayout = (RelativeLayout) findViewById(R.id.activity_gallery_progress_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.activity_gallery_error_layout);

		registerReceiver(receiver, new IntentFilter(DownloadService.broadcast));
		requestDownloadGallery();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_REFRESH, 0, getString(R.string.update));
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		Cursor c = (Cursor) av.getAdapter().getItem(position);

		// Opens gallery details when clicking an item in the list
		Intent intent = new Intent(this, GalleryActivityDetails.class);
		intent.putExtra("id", c.getString(c.getColumnIndex("id")));
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			// Downloads latest news
			requestDownloadGallery();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		// Gets images from database
		GalleryManager gm = new GalleryManager(this);
		Cursor c = gm.getFromDb();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.activity_gallery_image, c, c.getColumnNames(),
				new int[] { R.id.activity_gallery_image });

		GridView gridview = (GridView) findViewById(R.id.activity_gallery_gridview);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(this);

		// Resets new items counter
		GalleryManager.lastInserted = 0;
	}

	private void requestDownloadGallery() {
		if (Utils.isConnected(this)) {
			Intent service = new Intent(this, DownloadService.class);
			service.putExtra(Const.ACTION_EXTRA, Const.GALLERY);
			progressLayout.setVisibility(View.VISIBLE);
			startService(service);
		} else {
			errorLayout.setVisibility(View.VISIBLE);
			Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
		}
	}
}