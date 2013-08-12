package de.tum.in.tumcampusapp.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.CalendarSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.CalendarMapper;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Dialogs;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampusapp.models.managers.CalendarManager;

/**
 * Mock Activity to demonstrate the basic fragment based navigation using tabs.
 * 
 * @author Sascha Moecker
 * 
 */
public class CalendarActivity extends ActivityForAccessingTumOnline implements
		OnClickListener {
	public static final int MONTH_AFTER = 1;
	public static final int MONTH_BEFORE = 1;

	Calendar calendar = new GregorianCalendar();
	private CalendarManager calendarManager;
	private CalendarSectionsPagerAdapter mSectionsPagerAdapter;

	private ViewPager mViewPager;
	private SharedPreferences preferences;

	public CalendarActivity() {
		super(Const.CALENDER, R.layout.activity_calendar);
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

	public Uri addLocalCalendar() {
		ContentResolver crv = getContentResolver();
		Calendar calendar = Calendar.getInstance();
		Uri uri = CalendarMapper.addCalendar(calendar, crv);
		return uri;
	}

	public int deleteLocalCalendar() {
		ContentResolver crv = getContentResolver();
		Uri uri = Calendars.CONTENT_URI;
		int deleted = crv.delete(uri, " account_name = 'TUM_Campus_APP'", null);
		return deleted;

	}

	// displaying calendar
	public void displayCalendarOnGoogleCalendar() {
		// displaying Calendar
		Calendar beginTime = Calendar.getInstance();
		long startMillis = beginTime.getTimeInMillis();
		Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
		builder.appendPath("time");
		ContentUris.appendId(builder, startMillis);
		Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
		startActivity(intent);
	}

	/**
	 * Asynch taask for exporting the calendar to a local Google calendar
	 */
	public void exportCalendarToGoogle() {
		AsyncTask<Void, Void, Boolean> backgroundTask;

		backgroundTask = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {

				String calendarUri = preferences.getString(Const.CALENDAR_URI,
						"");

				// Deleting earlier calendar created by TUM Campus App
				deleteLocalCalendar();
				Uri uri = addLocalCalendar();
				addEvents(uri);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						CalendarActivity.this);
				builder.setMessage(getString(R.string.dialog_show_calendar))
						.setPositiveButton(getString(R.string.yes),
								CalendarActivity.this)
						.setNegativeButton(getString(R.string.no),
								CalendarActivity.this).show();
				attachSectionPagerAdapter();
				hideProgressLayout();
			}

			@Override
			protected void onPreExecute() {
				showProgressLayout();
			}
		};
		backgroundTask.execute();
	}

	public void deleteCalendarFromGoogle() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				CalendarActivity.this);
		builder.setMessage(getString(R.string.dialog_delete_calendar))
				.setPositiveButton(getString(R.string.yes),
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								int deleted = deleteLocalCalendar();
								if (deleted > 0)
									Toast.makeText(getApplicationContext(),
											R.string.calendar_deleted_toast,
											Toast.LENGTH_LONG).show();
								else
									Toast.makeText(
											getApplicationContext(),
											R.string.calendar_not_existing_toast,
											Toast.LENGTH_LONG).show();

							}
						})
				.setNegativeButton(getString(R.string.no),
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {

							}
						}).show();

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			displayCalendarOnGoogleCalendar();
		}
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

		mViewPager = (ViewPager) findViewById(R.id.pager);

		// Set the timespace between now and after this date and before this
		// Dates before the current date
		requestHandler.setParameter("pMonateVor", String.valueOf(MONTH_BEFORE));
		// Dates after the current date
		requestHandler.setParameter("pMonateNach", String.valueOf(MONTH_AFTER));

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		calendarManager = new CalendarManager(this);

		if (calendarManager.needsSync()) {
			super.requestFetch();
		} else {
			attachSectionPagerAdapter();
		}
	}

	/**
	 * Link the Sections with the content with a section adapter. Additionally
	 * put the current date at the start position.
	 */
	private void attachSectionPagerAdapter() {
		mSectionsPagerAdapter = new CalendarSectionsPagerAdapter(
				CalendarActivity.this, getSupportFragmentManager());

		mViewPager.setAdapter(mSectionsPagerAdapter);

		Date now = new Date();
		calendar.setTime(now);

		calendar.add(Calendar.MONTH, -CalendarActivity.MONTH_BEFORE);
		Date firstDate = calendar.getTime();

		long days = (now.getTime() - firstDate.getTime())
				/ (1000 * 60 * 60 * 24);
		Log.d("Days", String.valueOf(days));

		mViewPager.setCurrentItem((int) days);
	}

	/**
	 * Detach the adapter form the Pager to make the asynch task not conflicting
	 * with the UI thread.
	 */
	private void detachSectionPagerAdapter() {
		mViewPager.setAdapter(null);
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
				calendarManager.importKalendar(rawResponse);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				hideProgressLayout();
				attachSectionPagerAdapter();
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
			// Both features (Chart diagrams) are not available on older devices
			if (android.os.Build.VERSION.SDK_INT <= 10) {
				Dialogs.showAndroidVersionTooLowAlert(this);
				return true;
			}
			detachSectionPagerAdapter();
			exportCalendarToGoogle();
			return true;
		case R.id.action_delete_calendar:
			// Both features (Chart diagrams) are not available on older devices
			if (android.os.Build.VERSION.SDK_INT <= 10) {
				Dialogs.showAndroidVersionTooLowAlert(this);
				return true;
			}
			deleteCalendarFromGoogle();
			return true;
		default:
			detachSectionPagerAdapter();
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
	}
}
