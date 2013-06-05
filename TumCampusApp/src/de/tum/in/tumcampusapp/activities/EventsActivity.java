package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.managers.EventManager;

/**
 * Activity to show events (name, location, image, etc.)
 */
public class EventsActivity extends ActivityForDownloadingExternal implements OnItemClickListener, ViewBinder {

	public EventsActivity() {
		super(Const.EVENTS, R.layout.activity_events);
	}

	// TODO Implement that as a sliding tab
	private void getPastEvents() {
		EventManager em = new EventManager(this);
		Cursor cursor;
		SimpleCursorAdapter adapter;
		cursor = em.getPastFromDb();

		adapter = new SimpleCursorAdapter(EventsActivity.this, R.layout.activity_events_listview, cursor, cursor.getColumnNames(), new int[] { R.id.icon,
				R.id.name, R.id.infos });
		adapter.setViewBinder(EventsActivity.this);

		ListView lv2 = (ListView) findViewById(R.id.listView);
		lv2.setAdapter(adapter);
		lv2.setOnItemClickListener(EventsActivity.this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.requestDownload(false);
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		Cursor c = (Cursor) av.getAdapter().getItem(position);

		// open event details when clicking an event in the list
		Intent intent = new Intent(this, EventsDetailsActivity.class);
		intent.putExtra(Const.ID_EXTRA, c.getString(c.getColumnIndex(Const.ID_COLUMN)));
		startActivity(intent);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart() {
		super.onStart();

		// get current and upcoming events from database
		EventManager em = new EventManager(this);
		Cursor cursor;
		SimpleCursorAdapter adapter;

		cursor = em.getNextFromDb();

		adapter = new SimpleCursorAdapter(this, R.layout.activity_events_listview, cursor, cursor.getColumnNames(), new int[] { R.id.icon, R.id.name,
				R.id.infos });

		adapter.setViewBinder(this);

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);

		// reset new items counter
		EventManager.lastInserted = 0;
	}

	@Override
	public boolean setViewValue(View view, Cursor c, int index) {

		/**
		 * <pre>
		 * Show event info text as:
		 * Week-Day, Start DateTime - End Time
		 * location
		 * </pre>
		 */
		if (view.getId() == R.id.infos) {
			String[] weekDays = getString(R.string.week_splitted).split(",");

			TextView infos = (TextView) view;
			infos.setText(weekDays[c.getInt(c.getColumnIndex(Const.WEEKDAY_COLUMN))] + ", " + c.getString(c.getColumnIndex(Const.START_DE_COLUMN)) + " - "
					+ c.getString(c.getColumnIndex(Const.END_DE_COLUMN)) + "\n" + c.getString(c.getColumnIndex(Const.LOCATION_COLUMN)));
			return true;
		}
		return false;
	}
}