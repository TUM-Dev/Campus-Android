package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.NewsManager;
import de.tum.in.tumcampusapp.services.DownloadService;

/**
 * Activity to show News (message, image, date)
 */
public class NewsActivity extends Activity implements OnItemClickListener, ViewBinder {
	public final static int MENU_REFRESH = 0;
	Activity activity = this;
	private RelativeLayout errorLayout;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);
		progressLayout = (RelativeLayout) findViewById(R.id.activity_news_progress_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.activity_news_error_layout);

		registerReceiver(receiver, new IntentFilter(DownloadService.broadcast));
		requestDownloadNews();
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
	}

	@Override
	public void onItemClick(AdapterView<?> aview, View view, int position, long id) {
		ListView lv = (ListView) findViewById(R.id.activity_news_list_view);
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String url = c.getString(c.getColumnIndex(Const.LINK_COLUMN));

		if (url.length() == 0) {
			Toast.makeText(this, getString(R.string.no_link_existing), Toast.LENGTH_LONG).show();
			return;
		}

		// Opens Url in Browser
		Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(viewIntent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			// Downloads latest news
			requestDownloadNews();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Gets all news from database
		NewsManager nm = new NewsManager(this);
		Cursor c = nm.getAllFromDb();

		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.activity_news_listview, c, c.getColumnNames(), new int[] { R.id.image,
				R.id.message, R.id.date });

		adapter.setViewBinder(this);

		ListView lv = (ListView) findViewById(R.id.activity_news_list_view);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);

		// Resets new items counter
		NewsManager.lastInserted = 0;
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private void requestDownloadNews() {
		if (Utils.isConnected(this)) {
			Intent service = new Intent(this, DownloadService.class);
			service.putExtra(Const.ACTION_EXTRA, Const.NEWS);
			progressLayout.setVisibility(View.VISIBLE);
			startService(service);
		} else {
			errorLayout.setVisibility(View.VISIBLE);
			Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int index) {
		// Adds url (domain only) to date
		if (view.getId() == R.id.date) {
			String date = cursor.getString(index);
			String link = cursor.getString(cursor.getColumnIndex(Const.LINK_COLUMN));

			if (link.length() > 0) {
				TextView tv = (TextView) view;
				tv.setText(date + ", " + Uri.parse(link).getHost());
				return true;
			}
		}

		// hide empty view elements
		if (cursor.getString(index).length() == 0) {
			view.setVisibility(View.GONE);

			// no binding needed
			return true;
		}
		view.setVisibility(View.VISIBLE);
		return false;
	}
}