package de.tum.in.tumcampus.activities;

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
import android.os.Build;
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

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.adapters.CalendarSectionsPagerAdapter;
import de.tum.in.tumcampus.auxiliary.CalendarMapper;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Dialogs;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.models.managers.CalendarManager;

/**
 * Activity shwoing the user's calendar. Calendar items (events) are fetched from TUMOnline and displayed as blocks on a timeline.
 * 
 * @author Sascha Moecker
 * @test GitHub Test
 * 
 */
public class CalendarActivity extends ActivityForAccessingTumOnline implements OnClickListener {
	// The space between the first and the last date
	public static final int MONTH_AFTER = 1;
	public static final int MONTH_BEFORE = 0;

	private Calendar calendar = new GregorianCalendar();

	private CalendarManager calendarManager;

    private ViewPager mViewPager;

	// Objects for disabling or enabling the options menu items
	private MenuItem menuItemExportGoogle;
	private MenuItem menuItemDeleteCalendar;

    /** Used as a flag, if there are results fetched from internet */
	private boolean isFetched;

	public CalendarActivity() {
		super(Const.CALENDER, R.layout.activity_calendar);
	}

	@SuppressLint("InlinedApi")
	public void addEvents(Uri uri) {
		CalendarManager calendarManager = new CalendarManager(this);
		Date dtstart = null, dtend = null;

		// Get all calendar items from database
		Cursor cursor = calendarManager.getAllFromDb();
		while (cursor.moveToNext()) {
			// Get each table row
			final String status = cursor.getString(1);
			final String title = cursor.getString(3);
			final String description = cursor.getString(4);
			final String strstart = cursor.getString(5);
			final String strend = cursor.getString(6);
			final String location = cursor.getString(7);

			if (!status.equals("CANCEL")) {
				try {
					// Get the correct date and time from database
					dtstart = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(strstart);
					dtend = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(strend);

					Calendar beginTime = Calendar.getInstance();
					beginTime.setTime(dtstart);
					Calendar endTime = Calendar.getInstance();
					endTime.setTime(dtend);

					// Get start and end time
					long startMillis = beginTime.getTimeInMillis();
					long endMillis = endTime.getTimeInMillis();

					ContentResolver contentResolver = this.getContentResolver();
					ContentValues values = new ContentValues();

					// Put the received values into a contentResolver to
					// transmit the to Google Calendar
					values.put(Events.DTSTART, startMillis);
					values.put(Events.DTEND, endMillis);
					values.put(Events.TITLE, title);
					values.put(Events.DESCRIPTION, description);
					values.put(Events.CALENDAR_ID, this.getID(uri));
					values.put(Events.EVENT_LOCATION, location);
					values.put(Events.EVENT_TIMEZONE, R.string.calendarTimeZone);
					contentResolver.insert(Events.CONTENT_URI, values);

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Adds an new local Google calendar
	 * 
	 * @return the URI to access the calendar
	 */
	public Uri addLocalCalendar() {
		ContentResolver crv = this.getContentResolver();
		Calendar calendar = Calendar.getInstance();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		CalendarMapper calendarMapper = new CalendarMapper(this.getString(R.string.calendar_account_name), this.getString(R.string.calendar_display_name),
				preferences);

        return calendarMapper.addCalendar(calendar, crv);
	}

	/**
	 * Link the Sections with the content with a section adapter. Additionally put the current date at the start position.
	 */
	private void attachSectionPagerAdapter() {
        CalendarSectionsPagerAdapter mSectionsPagerAdapter = new CalendarSectionsPagerAdapter(CalendarActivity.this, this.getSupportFragmentManager());

		this.mViewPager.setAdapter(mSectionsPagerAdapter);

		Date now = new Date();
		this.calendar.setTime(now);

		this.calendar.add(Calendar.MONTH, -CalendarActivity.MONTH_BEFORE);
		Date firstDate = this.calendar.getTime();

		long days = (now.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24);
		Log.d("Days", String.valueOf(days));

		this.mViewPager.setCurrentItem((int) days);
	}

	/**
	 * Asynch task for deleting the calendar from local Google calendar
	 */
	public void deleteCalendarFromGoogle() {
		AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
		builder.setMessage(this.getString(R.string.dialog_delete_calendar)).setPositiveButton(this.getString(R.string.yes), new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				int deleted = CalendarActivity.this.deleteLocalCalendar();
				if (deleted > 0) {
					Toast.makeText(CalendarActivity.this.getApplicationContext(), R.string.calendar_deleted_toast, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(CalendarActivity.this.getApplicationContext(), R.string.calendar_not_existing_toast, Toast.LENGTH_LONG).show();
				}

			}
		}).setNegativeButton(this.getString(R.string.no), new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {

			}
		}).show();
	}

	/**
	 * Deletes a local Google calendar
	 * 
	 * @return The calendars id
	 */
	public int deleteLocalCalendar() {
		ContentResolver crv = this.getContentResolver();
		Uri uri = Calendars.CONTENT_URI;
        return crv.delete(uri, " account_name = '" + this.getString(R.string.calendar_account_name) + "'", null);
	}

	/**
	 * Detach the adapter form the Pager to make the asynch task not conflicting with the UI thread.
	 */
	private void detachSectionPagerAdapter() {
		this.mViewPager.setAdapter(null);
	}

	/**
	 * Starts the Google calendar Activity to display the exported calendar.
	 */
	public void displayCalendarOnGoogleCalendar() {
		// displaying Calendar
		Calendar beginTime = Calendar.getInstance();
		long startMillis = beginTime.getTimeInMillis();
		Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
		builder.appendPath("time");
		ContentUris.appendId(builder, startMillis);
		Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
		this.startActivity(intent);
	}

	/**
	 * Asynch task for exporting the calendar to a local Google calendar
	 */
	public void exportCalendarToGoogle() {
		AsyncTask<Void, Void, Boolean> backgroundTask;

		backgroundTask = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				// Deleting earlier calendar created by TUM Campus App
				CalendarActivity.this.deleteLocalCalendar();
				Uri uri = CalendarActivity.this.addLocalCalendar();
				CalendarActivity.this.addEvents(uri);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				// Informs the user about the ongoing action
				if (!CalendarActivity.this.isFinishing()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
					builder.setMessage(CalendarActivity.this.getString(R.string.dialog_show_calendar))
							.setPositiveButton(CalendarActivity.this.getString(R.string.yes), CalendarActivity.this)
							.setNegativeButton(CalendarActivity.this.getString(R.string.no), CalendarActivity.this).show();
					CalendarActivity.this.attachSectionPagerAdapter();
					CalendarActivity.this.hideProgressLayout();
				}
			}

			@Override
			protected void onPreExecute() {
				CalendarActivity.this.showProgressLayout();
			}
		};
		backgroundTask.execute();
	}

	/**
	 * Get added calendar id.
	 * 
	 * @param uri
	 *            the URI to be received
	 * @return the calendar's id
	 */
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
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			this.displayCalendarOnGoogleCalendar();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getBoolean("implicitly_id", true)) {
			ImplicitCounter.Counter("calender_id", this.getApplicationContext());
		}

		this.mViewPager = (ViewPager) this.findViewById(R.id.pager);

		// Set the timespace between now and after this date and before this
		// Dates before the current date
		this.requestHandler.setParameter("pMonateVor", String.valueOf(MONTH_BEFORE));
		// Dates after the current date
		this.requestHandler.setParameter("pMonateNach", String.valueOf(MONTH_AFTER));

		this.calendarManager = new CalendarManager(this);

		if (this.calendarManager.needsSync()) {
			super.requestFetch();
		} else {
			this.attachSectionPagerAdapter();
			this.isFetched = true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.menu_activity_calendar, menu);
		return true;
	}

	@Override
	public void onFetch(final String rawResponse) {
		// parsing and saving xml response
		AsyncTask<Void, Void, Boolean> backgroundTask;
		backgroundTask = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {

				// Remove cache before importing the new items
				CalendarActivity.this.calendarManager.removeCache();

				// Do the import
				CalendarActivity.this.calendarManager.importKalendar(rawResponse);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				CalendarActivity.this.hideProgressLayout();
				CalendarActivity.this.attachSectionPagerAdapter();
				// update the action bar to display the enabled menu options
				if (Build.VERSION.SDK_INT >= 11) {
					CalendarActivity.this.invalidateOptionsMenu();
				}
			}

			@Override
			protected void onPreExecute() {
				CalendarActivity.this.showProgressLayout();
				CalendarActivity.this.isFetched = true;
			}
		};
		backgroundTask.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_export_calendar:
			// the Calendar export is not supported for API < 11
			if (android.os.Build.VERSION.SDK_INT <= 10) {
				Dialogs.showAndroidVersionTooLowAlert(this);
				return true;
			}
			this.detachSectionPagerAdapter();
			this.exportCalendarToGoogle();
			return true;
		case R.id.action_delete_calendar:
			// deleting calendars is not supported for API < 11
			if (android.os.Build.VERSION.SDK_INT <= 10) {
				Dialogs.showAndroidVersionTooLowAlert(this);
				return true;
			}
			this.deleteCalendarFromGoogle();
			return true;
		default:
			this.detachSectionPagerAdapter();
			this.isFetched = false;
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.menuItemExportGoogle = menu.findItem(R.id.action_export_calendar);
		this.menuItemDeleteCalendar = menu.findItem(R.id.action_delete_calendar);
		this.setMenuEnabled(this.isFetched);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Enabled the menu items which are not commonly accessible.
	 * 
	 * @param enabled
	 */
	public void setMenuEnabled(boolean enabled) {
		this.menuItemExportGoogle.setEnabled(enabled);
		this.menuItemDeleteCalendar.setEnabled(enabled);
	}
}
