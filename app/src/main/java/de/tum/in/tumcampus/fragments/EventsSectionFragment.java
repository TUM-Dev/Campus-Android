package de.tum.in.tumcampus.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.EventsDetailsActivity;
import de.tum.in.tumcampus.adapters.EventsSectionsPagerAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.managers.EventManager;

/**
 * Fragment for each category-page.
 */
public class EventsSectionFragment extends Fragment implements
		OnItemClickListener, ViewBinder {
	private Activity activity;

    public EventsSectionFragment() {
	}

	@SuppressWarnings({ "deprecation" })
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_events_section,
				container, false);

		activity = getActivity();
        ListView listView = (ListView) rootView.findViewById(R.id.listView);
        RelativeLayout errorLayout = (RelativeLayout) rootView.findViewById(R.id.error_layout);

		int eventMode = getArguments().getInt(
				EventsSectionsPagerAdapter.ARG_EVENT_MODE);

		// get current and upcoming events from database
		EventManager em = new EventManager(activity);
		Cursor cursor;
		SimpleCursorAdapter adapter;

		switch (eventMode) {
		case EventsSectionsPagerAdapter.PAGE_LATESTS_EVENTS:
			cursor = em.getNextFromDb();
			break;
		case EventsSectionsPagerAdapter.PAGE_PAST_EVENTS:
			cursor = em.getPastFromDb();
			break;
		default:
			cursor = em.getNextFromDb();
			break;

		}

		activity.startManagingCursor(cursor);

		if (cursor.getCount() > 0) {
			adapter = new SimpleCursorAdapter(activity,
					R.layout.activity_events_listview, cursor,
					cursor.getColumnNames(), new int[] { R.id.icon, R.id.name,
							R.id.infos });

			adapter.setViewBinder(this);

			listView.setAdapter(adapter);
			listView.setOnItemClickListener(this);

			// reset new items counter
			EventManager.lastInserted = 0;
		} else {
			errorLayout.setVisibility(View.VISIBLE);
		}

		return rootView;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		Cursor cursor = (Cursor) av.getAdapter().getItem(position);
		activity.startManagingCursor(cursor);

		// open event details when clicking an event in the list
		Intent intent = new Intent(activity, EventsDetailsActivity.class);
		intent.putExtra(Const.ID_EXTRA,
				cursor.getString(cursor.getColumnIndex(Const.ID_COLUMN)));
		startActivity(intent);
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
			infos.setText(weekDays[c.getInt(c
					.getColumnIndex(Const.WEEKDAY_COLUMN))]
					+ ", "
					+ c.getString(c.getColumnIndex(Const.START_DE_COLUMN))
					+ " - "
					+ c.getString(c.getColumnIndex(Const.END_DE_COLUMN))
					+ "\n"
					+ c.getString(c.getColumnIndex(Const.LOCATION_COLUMN)));
			return true;
		}
		return false;
	}
}