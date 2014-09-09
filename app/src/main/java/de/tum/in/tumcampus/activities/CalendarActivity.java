package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.adapters.CalendarSectionsPagerAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.models.managers.CalendarManager;

/**
 * Activity shwoing the user's calendar. Calendar items (events) are fetched from TUMOnline and displayed as blocks on a timeline.
 * 
 * @author Sascha Moecker
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
				int deleted = CalendarManager.deleteLocalCalendar(CalendarActivity.this);
                SharedPreferences prefs = getSharedPreferences(Const.INTERNAL_PREFS, 0);
                prefs.edit().putBoolean(Const.SYNC_CALENDAR, false).apply();
                invalidateOptionsMenu();
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
                CalendarManager.syncCalendar(CalendarActivity.this);
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

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			this.displayCalendarOnGoogleCalendar();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
			detachSectionPagerAdapter();
			exportCalendarToGoogle();

            // Enable automatic calendar synchronisation
            SharedPreferences prefs = getSharedPreferences(Const.INTERNAL_PREFS, 0);
            prefs.edit().putBoolean(Const.SYNC_CALENDAR, true).apply();
            supportInvalidateOptionsMenu();
            return true;
		case R.id.action_delete_calendar:
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
        // the Calendar export is not supported for API < 11
        if (android.os.Build.VERSION.SDK_INT < 11) {
            menuItemExportGoogle.setVisible(false);
            menuItemDeleteCalendar.setVisible(false);
        } else {
            this.menuItemExportGoogle = menu.findItem(R.id.action_export_calendar);
            this.menuItemDeleteCalendar = menu.findItem(R.id.action_delete_calendar);
            this.setMenuEnabled(this.isFetched);

            SharedPreferences prefs = getSharedPreferences(Const.INTERNAL_PREFS, 0);
            boolean bed = prefs.getBoolean(Const.SYNC_CALENDAR, false);
            menuItemExportGoogle.setVisible(!bed);
            menuItemDeleteCalendar.setVisible(bed);
        }
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Enabled the menu items which are not commonly accessible.
	 */
	public void setMenuEnabled(boolean enabled) {
		this.menuItemExportGoogle.setEnabled(enabled);
		this.menuItemDeleteCalendar.setEnabled(enabled);
	}
}
