package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.managers.NewsManager;

/**
 * Activity to show News (message, image, date)
 */
public class NewsActivity extends ActivityForDownloadingExternal implements
		OnItemClickListener, ViewBinder {
	RelativeLayout overlay;
	ImageView overlay_cross;
	SharedPreferences sharedPrefs;
	static int layoutID;
	static {
		if (Build.VERSION.SDK_INT >= 11)
			layoutID = R.layout.activity_news_overlay_actionbar;
		else
			layoutID = R.layout.activity_news_overlay_menubutton;
	}

	public NewsActivity() {
		super(Const.NEWS, layoutID);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.requestDownload(false);
		
		overlay = (RelativeLayout) findViewById(R.id.tumNews_overlay);
		overlay_cross = (ImageView) findViewById(R.id.tumNews_cross);
		//Counting the number of times that the user used this activity for intelligent reordering 
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getBoolean("implicitly_id", true)){
								ImplicitCounter.Counter("tum_news_id",getApplicationContext());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> aview, View view, int position,
			long id) {
		ListView lv = (ListView) findViewById(R.id.activity_news_list_view);

		Cursor cursor = (Cursor) lv.getAdapter().getItem(position);
		startManagingCursor(cursor);

		String url = cursor.getString(cursor.getColumnIndex(Const.LINK_COLUMN));

		if (url.length() == 0) {
			Toast.makeText(this, getString(R.string.no_link_existing),
					Toast.LENGTH_LONG).show();
			return;
		}

		// Opens Url in Browser
		Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(viewIntent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		overlay_cross.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sharedPrefs.edit().putBoolean(Const.FIRST_RUN, false).commit();
				overlay.setVisibility(View.GONE);
			}
		});
		if (sharedPrefs.getBoolean(Const.FIRST_RUN, true))
			overlay.setVisibility(View.VISIBLE);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart() {
		super.onStart();

		// Gets all news from database
		NewsManager nm = new NewsManager(this);
		Cursor cursor = nm.getAllFromDb();
		startManagingCursor(cursor);
		if (cursor.getCount() > 0) {
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
					R.layout.activity_news_listview, cursor,
					cursor.getColumnNames(), new int[] { R.id.image,
							R.id.message, R.id.date });

			adapter.setViewBinder(this);

			ListView lv = (ListView) findViewById(R.id.activity_news_list_view);
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(this);

			// Resets new items counter
			NewsManager.lastInserted = 0;
		} else {
			super.showErrorLayout();
		}
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int index) {
		// Adds url (domain only) to date
		if (view.getId() == R.id.date) {
			String date = cursor.getString(index);
			String link = cursor.getString(cursor
					.getColumnIndex(Const.LINK_COLUMN));

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