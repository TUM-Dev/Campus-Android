package de.tum.in.tumcampusapp.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.CalendarSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.CalendarMapper;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampusapp.models.managers.CalendarManager;

/**
 * Mock Activity to demonstrate the basic fragment based navigation using tabs.
 * 
 * @author Sascha Moecker
 * 
 */
public class CalendarActivity extends ActivityForAccessingTumOnline {
	public static final int MONTH_AFTER = 1;
	public static final int MONTH_BEFORE = 1;

	private CalendarManager kalMgr;
	private CalendarSectionsPagerAdapter mSectionsPagerAdapter;

	private ViewPager mViewPager;
	private SharedPreferences preferences;
	private Uri uri;

	public CalendarActivity() {
		super(Const.CALENDER, R.layout.activity_mockcalendar);
	}

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	public void addEvents(Uri uri) {
		CalendarManager kalMgr = new CalendarManager(this);
		Date dtstart = null, dtend = null;

		Cursor cursor = kalMgr.getAllFromDb();
		while (cursor.moveToNext()) {
			final String nr = cursor.getString(0);
			final String status = cursor.getString(1);
			final String url = cursor.getString(2);
			final String title = cursor.getString(3);
			final String description = cursor.getString(4);
			final String strstart = cursor.getString(5);
			final String strend = cursor.getString(6);
			final String location = cursor.getString(7);
			final String longitude = cursor.getString(8);
			final String latitude = cursor.getString(9);
			if (!status.equals("CANCEL")) {
				try {
					dtstart = new SimpleDateFormat("yyyy-MM-dd HH:mm",
							Locale.ENGLISH).parse(strstart);
					dtend = new SimpleDateFormat("yyyy-MM-dd HH:mm",
							Locale.ENGLISH).parse(strend);
					Calendar beginTime = Calendar.getInstance();
					beginTime.setTime(dtstart);
					Calendar endTime = Calendar.getInstance();
					endTime.setTime(dtend);

					long startMillis = beginTime.getTimeInMillis();
					long endMillis = endTime.getTimeInMillis();
					ContentResolver cr = getContentResolver();
					ContentValues values = new ContentValues();
					values.put(Events.DTSTART, startMillis);
					values.put(Events.DTEND, endMillis);
					values.put(Events.TITLE, title);
					values.put(Events.DESCRIPTION, description);
					values.put(Events.CALENDAR_ID, getID(uri));
					values.put(Events.EVENT_TIMEZONE, "America/Los_Angeles");
					Uri uriInsert = cr.insert(Events.CONTENT_URI, values);

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void addToCalendar() {
		ContentResolver crv = getContentResolver();
		Calendar calendar = Calendar.getInstance();
		uri = CalendarMapper.addCalendar(calendar, crv);
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = settings.edit();
		editor.putString(Const.CALENDAR_URI, uri.toString());
		editor.commit();
	}

	public void delCal(String strKalUri) {
		Uri kalUri = Uri.parse(strKalUri);
		ContentResolver crv = getContentResolver();
		crv.delete(kalUri, null, null);
	}

	// displaying calendar
	public void displayCal() {
		// displaying Calendar
		Calendar beginTime = Calendar.getInstance();
		long startMillis = beginTime.getTimeInMillis();
		Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
		builder.appendPath("time");
		ContentUris.appendId(builder, startMillis);
		Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
		startActivity(intent);
	}

	public void exportCalendarToGoogle() {
		AsyncTask<Void, Void, Boolean> backgroundTask;
		backgroundTask = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {

				String strKalUri = preferences
						.getString(Const.CALENDAR_URI, "");

				// checking if calendar already exist in user's device
				if (strKalUri == "")
					addToCalendar();
				else {
					delCal(strKalUri);
					addToCalendar();
				}
				addEvents(uri);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				displayCal();
				hideProgressLayout();
			}

			@Override
			protected void onPreExecute() {
				showProgressLayout();
			}
		};
		backgroundTask.execute();
	}

	// get added calendar id
	public String getID(Uri uri) {
		String[] projection = new String[] { "_id", "name" };
		ContentResolver ctnresolver = this.getContentResolver();
		Cursor cursor = ctnresolver.query(uri, projection, null, null, null);
		String idstring = "0";
		while (cursor.moveToNext()) {
			idstring = cursor.getString(0);
		}
		return idstring;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSectionsPagerAdapter = new CalendarSectionsPagerAdapter(this,
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// Set the timespace between now and after this date and before this
		// Dates before the current date
		requestHandler.setParameter("pMonateVor", String.valueOf(MONTH_BEFORE));
		// Dates after the current date
		requestHandler.setParameter("pMonateNach", String.valueOf(MONTH_AFTER));

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		kalMgr = new CalendarManager(this);

		if (kalMgr.needsSync()) {
			super.requestFetch();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_schedule_activity, menu);
		return true;
	}

	@Override
	public void onFetch(final String rawResponse) {
		// parsing and saving xml response
		AsyncTask<Void, Void, Boolean> backgroundTask;
		backgroundTask = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				kalMgr.importKalendar(rawResponse);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				hideProgressLayout();
			}

			@Override
			protected void onPreExecute() {
				showProgressLayout();
			}
		};
		backgroundTask.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_export_calendar:
			exportCalendarToGoogle();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
	}
}
