package de.tum.in.tumcampusapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.LectureItemManager;
import de.tum.in.tumcampusapp.preferences.UserPreferencesActivity;
import de.tum.in.tumcampusapp.services.ImportService;

public class LectureScheduleActivity extends ActivityForAccessingTumOnline
		implements OnItemClickListener, ViewBinder {
	AccessTokenManager accessTokenManager = new AccessTokenManager(this);
	CalendarView calendar;
	Context context = this;
	ListView listView;

	/**
	 * Receiver for Services
	 */
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(getClass().getSimpleName(), "Received broadcast action: "
					+ intent.getAction());
			if (intent.getAction().equals(ImportService.BROADCAST_NAME)) {
				String action = intent.getStringExtra(Const.ACTION_EXTRA);

				if (action.equals(Const.LECTURES_TUM_ONLINE_START)) {
					progressLayout.setVisibility(View.VISIBLE);
				}
				if (action.equals(Const.LECTURES_TUM_ONLINE_FINISH)) {
					progressLayout.setVisibility(View.GONE);
					// Show error Sign if is nothing to display
					if (!updateListWithLectures()) {
						errorLayout.setVisibility(View.VISIBLE);
					}
				}
			}
		}
	};

	public LectureScheduleActivity() {
		super(Const.FETCH_NOTHING, R.layout.activity_lecturesschedule);
	}

	public void onClick(View view) {
		int viewId = view.getId();
		switch (viewId) {
		case R.id.failed_layout:
			failedTokenLayout.setVisibility(View.GONE);
			requestImport(true);
			break;
		case R.id.no_token_layout:
			Intent intent = new Intent(this, UserPreferencesActivity.class);
			startActivity(intent);
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Registers receiver for download and import
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ImportService.BROADCAST_NAME);
		registerReceiver(receiver, intentFilter);

		listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(this);

		requestImport(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	public void onFetch(String rawResponse) {
		progressLayout.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {

		// click on lecture unit list
		Cursor c = (Cursor) listView.getAdapter().getItem(position);
		String url = c.getString(c.getColumnIndex(Const.URL_COLUMN));

		// set bundle for LectureDetails and show it
		Bundle bundle = new Bundle();
		// we need the stp_sp_nr
		bundle.putString("stp_sp_nr",
				c.getString(c.getColumnIndex("lectureId")));
		Intent intent = new Intent(this, LecturesDetailsActivity.class);
		intent.putExtras(bundle);
		// start LectureDetails for given stp_sp_nr
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_update:
			requestImport(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void requestImport(boolean force) {
		errorLayout.setVisibility(View.GONE);
		noTokenLayout.setVisibility(View.GONE);
		failedTokenLayout.setVisibility(View.GONE);
		progressLayout.setVisibility(View.GONE);

		// Get access token
		String accessToken = PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						Const.ACCESS_TOKEN, null);

		// Check token if null
		if (Utils.isAccessTokenValid(accessToken)) {
			if (Utils.isConnected(this)) {
				Intent service = new Intent(this, ImportService.class);
				service.putExtra(Const.ACTION_EXTRA, Const.LECTURES_TUM_ONLINE);
				service.putExtra(Const.FORCE_DOWNLOAD, force);
				startService(service);

			} else {
				Toast.makeText(this, R.string.no_internet_connection,
						Toast.LENGTH_SHORT).show();
				// Show error Sign if is nothing to display
				if (!updateListWithLectures()) {
					errorLayout.setVisibility(View.VISIBLE);
				}
			}
		} else {
			Log.i(getClass().getSimpleName(), "No token was set");
			noTokenLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * change presentation of lecture units in the list
	 */
	@Override
	public boolean setViewValue(View view, Cursor c, int index) {
		String[] weekDays = getString(R.string.week_splitted).split(",");

		if (view.getId() == android.R.id.text1) {
			// truncate lecture name to 20 characters,
			// append lecture unit note
			String name = c.getString(c.getColumnIndex(Const.NAME_COLUMN));
			String note = c.getString(c.getColumnIndex(Const.NOTE_COLUMN));
			if (note.length() > 0) {
				note = " - " + note;
			}
			TextView tv = (TextView) view;

			tv.setText(name + note);
			return true;
		}
		if (view.getId() == android.R.id.text2) {
			/**
			 * <pre>
			 * show info as:
			 * Lecture: Week-day, Start DateTime - End Time, Room-Nr-Intern
			 * Holiday: Week-day, Start Date
			 * vacation info: Start Date - End Date
			 * 
			 * Location format: Room-Nr-Intern, Room-name (Room-Nr-Extern)
			 * </pre>
			 */
			String info = "";
			String lectureId = c.getString(c
					.getColumnIndex(Const.LECTURE_ID_COLUMN));
			// TODO IMPORTANT Check whether "start_dt" and "start_de" are
			// actually the same
			if (lectureId.equals(Const.VACATION)) {
				info = c.getString(c.getColumnIndex(Const.START_DT_COLUMN))
						+ " - " // TODO REVIEW Vasyl changed start to end
						+ c.getString(c.getColumnIndex(Const.END_DT_COLUMN));

			} else if (lectureId.equals(Const.HOLIDAY)) {
				info = weekDays[c
						.getInt(c.getColumnIndex(Const.WEEKDAY_COLUMN))]
						+ ", "
						+ c.getString(c.getColumnIndex(Const.START_DT_COLUMN));

			} else {
				info = weekDays[c
						.getInt(c.getColumnIndex(Const.WEEKDAY_COLUMN))]
						+ ", "
						+ c.getString(c.getColumnIndex(Const.START_DE_COLUMN))
						+ " - " // TODO REVIEW Vasyl changed start to end
						+ c.getString(c.getColumnIndex(Const.END_DE_COLUMN));

				String location = c.getString(c
						.getColumnIndex(Const.LOCATION_COLUMN));
				if (location.indexOf(",") != -1) {
					location = location.substring(0, location.indexOf(","));
				}
				if (location.length() != 0) {
					info += ", " + location;
				}
			}
			TextView tv = (TextView) view;
			tv.setText(info);
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean updateListWithLectures() {
		// get all upcoming lecture units
		LectureItemManager lim = new LectureItemManager(this);

		// Cursor cursor = lim.getRecentFromDb();
		Cursor cursor = lim.getAllFromDb();

		startManagingCursor(cursor);
		if (cursor.getCount() == 0) {
			return false;
		}

		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.list_layout_two_line_item, cursor,
				cursor.getColumnNames(), new int[] { android.R.id.text1,
						android.R.id.text2 });

		adapter.setViewBinder(this);

		listView.setAdapter(adapter);
		return true;
	}
}
