package de.tum.in.tumcampusapp.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.Handler;
import android.os.Message;
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
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.CalendarSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.CalendarMapper;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Dialogs;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampusapp.models.managers.CalendarManager;

/**
 * Activity shwoing the user's calendar. Calendar items (events) are fetched
 * from TUMOnline and displayed as blocks on a timeline.
 * 
 * @author Sascha Moecker
 * @test GitHub Test
 * 
 */
public class CalendarActivity extends ActivityForAccessingTumOnline implements
		OnClickListener {
	// The space between the first and the last date
	public static final int MONTH_AFTER = 1;
	public static final int MONTH_BEFORE = 0;

	private Calendar calendar = new GregorianCalendar();

	private CalendarManager calendarManager;
	private CalendarSectionsPagerAdapter mSectionsPagerAdapter;

	private ViewPager mViewPager;

	// Objects for disabling or enabling the options menu items
	private MenuItem menuItemExportGoogle;
	private MenuItem menuItemDeleteCalendar;
	
	private SharedPreferences sharedPrefs;

	/** Used as a flag, if there are results fetched from internet */
	private boolean isFetched;

	public CalendarActivity() {
		super(Const.CALENDER, R.layout.activity_calendar);
	}

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
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
					dtstart = new SimpleDateFormat("yyyy-MM-dd HH:mm",
							Locale.ENGLISH).parse(strstart);
					dtend = new SimpleDateFormat("yyyy-MM-dd HH:mm",
							Locale.ENGLISH).parse(strend);

					Calendar beginTime = Calendar.getInstance();
					beginTime.setTime(dtstart);
					Calendar endTime = Calendar.getInstance();
					endTime.setTime(dtend);

					// Get start and end time
					long startMillis = beginTime.getTimeInMillis();
					long endMillis = endTime.getTimeInMillis();

					ContentResolver contentResolver = getContentResolver();
					ContentValues values = new ContentValues();

					// Put the received values into a contentResolver to
					// transmit the to Google Calendar
					values.put(Events.DTSTART, startMillis);
					values.put(Events.DTEND, endMillis);
					values.put(Events.TITLE, title);
					values.put(Events.DESCRIPTION, description);
					values.put(Events.CALENDAR_ID, getID(uri));
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
		ContentResolver crv = getContentResolver();
		Calendar calendar = Calendar.getInstance();
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		CalendarMapper calendarMapper = new CalendarMapper(
				getString(R.string.calendar_account_name),
				getString(R.string.calendar_display_name), preferences);

		Uri uri = calendarMapper.addCalendar(calendar, crv);
		return uri;
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
	 * Asynch task for deleting the calendar from local Google calendar
	 */
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

	/**
	 * Deletes a local Google calendar
	 * 
	 * @return The calendars id
	 */
	public int deleteLocalCalendar() {
		ContentResolver crv = getContentResolver();
		Uri uri = Calendars.CONTENT_URI;
		int deleted = crv.delete(uri, " account_name = '"
				+ getString(R.string.calendar_account_name) + "'", null);
		return deleted;
	}

	/**
	 * Detach the adapter form the Pager to make the asynch task not conflicting
	 * with the UI thread.
	 */
	private void detachSectionPagerAdapter() {
		mViewPager.setAdapter(null);
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
		startActivity(intent);
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
				deleteLocalCalendar();
				Uri uri = addLocalCalendar();
				addEvents(uri);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				// Informs the user about the ongoing action
				if (!isFinishing()) {
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
			}

			@Override
			protected void onPreExecute() {
				showProgressLayout();
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
			displayCalendarOnGoogleCalendar();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getBoolean("implicitly_id", true)){
			ImplicitCounter.Counter("calender_id",getApplicationContext());
		}

		mViewPager = (ViewPager) findViewById(R.id.pager);

		// Set the timespace between now and after this date and before this
		// Dates before the current date
		requestHandler.setParameter("pMonateVor", String.valueOf(MONTH_BEFORE));
		// Dates after the current date
		requestHandler.setParameter("pMonateNach", String.valueOf(MONTH_AFTER));

		calendarManager = new CalendarManager(this);

		if (calendarManager.needsSync()) {
			super.requestFetch();
		} else {
			attachSectionPagerAdapter();
			isFetched = true;
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity_calendar, menu);
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
				// update the action bar to display the enabled menu options
				if (Build.VERSION.SDK_INT >= 11) {
					invalidateOptionsMenu();
				}
			}

			@Override
			protected void onPreExecute() {
				showProgressLayout();
				isFetched = true;
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
			detachSectionPagerAdapter();
			exportCalendarToGoogle();
			return true;
		case R.id.action_delete_calendar:
			// deleting calendars is not supported for API < 11
			if (android.os.Build.VERSION.SDK_INT <= 10) {
				Dialogs.showAndroidVersionTooLowAlert(this);
				return true;
			}
			deleteCalendarFromGoogle();
			return true;
		default:
			detachSectionPagerAdapter();
			isFetched = false;
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menuItemExportGoogle = menu.findItem(R.id.action_export_calendar);
		menuItemDeleteCalendar = menu.findItem(R.id.action_delete_calendar);
		setMenuEnabled(isFetched);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
	}

	/**
	 * Enabled the menu items which are not commonly accessible.
	 * 
	 * @param enabled
	 */
	public void setMenuEnabled(boolean enabled) {
		menuItemExportGoogle.setEnabled(enabled);
		menuItemDeleteCalendar.setEnabled(enabled);
	}
}
