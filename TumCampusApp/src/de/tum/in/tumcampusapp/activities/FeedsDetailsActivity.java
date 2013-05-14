package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.managers.FeedItemManager;

/**
 * Activity to show RSS-feeds and their news items
 */
public class FeedsDetailsActivity extends ActivityForDownloadingExternal implements ViewBinder, OnItemClickListener {
	private static String feedId;
	private static String feedName;

	public FeedsDetailsActivity() {
		super(Const.FEEDS, R.layout.activity_feedsdetails);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		feedId = getIntent().getExtras().getString(Const.FEED_ID);
		feedName = getIntent().getExtras().getString(Const.FEED_NAME);

		Bundle extras = new Bundle();
		extras.putInt(Const.FEED_ID, Integer.valueOf(feedId));
		super.requestDownloadWithExtras(extras);
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		// click on feed item in list, open URL in browser
		if (av.getId() == R.id.listView) {
			Cursor c = (Cursor) av.getAdapter().getItem(position);
			String link = c.getString(c.getColumnIndex(Const.LINK_COLUMN));

			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			startActivity(intent);
			return;
		}
	}

	// Override this method, because an update requires some more information in
	// a bundle.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			Bundle extras = new Bundle();
			extras.putInt(Const.FEED_ID, Integer.valueOf(feedId));
			super.requestDownloadWithExtras(extras);
			return true;
		default:
			return false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		setTitle(getString(R.string.rss_feeds) + " for " + feedName);

		// get all feed items for a feed
		FeedItemManager fim = new FeedItemManager(this);
		Cursor cursor = fim.getAllFromDb(feedId);

		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.activity_feeds_listview, cursor, cursor.getColumnNames(), new int[] { R.id.icon,
				R.id.title, R.id.description });

		adapter.setViewBinder(this);
		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int index) {
		// hide empty view elements (e.g. missing image or description)
		if (cursor.getString(index).length() == 0) {
			view.setVisibility(View.GONE);

			// no binding needed
			return true;
		}
		view.setVisibility(View.VISIBLE);
		return false;
	}
}