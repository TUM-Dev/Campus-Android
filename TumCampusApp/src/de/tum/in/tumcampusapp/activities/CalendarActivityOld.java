package de.tum.in.tumcampusapp.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.CalendarMapper;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.CalendarManager;

public class CalendarActivityOld extends ActivityForAccessingTumOnline {
	Date currentDate = new Date();
	DatePicker datePicker;
	private final ArrayList<RelativeLayout> eventList = new ArrayList<RelativeLayout>();
	private RelativeLayout eventView;
	CalendarManager kalMgr;
	RelativeLayout mainScheduleLayout;
	SharedPreferences preferences;
	String rawResponse;

	Date requestedDate;
	Uri uri;

	public CalendarActivityOld() {
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

	private RelativeLayout inflateEventView() {
		LayoutInflater layoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return (RelativeLayout) layoutInflater.inflate(
				R.layout.layout_time_entry, null);
	}

	private LayoutParams initLayoutParams(float hours) {
		int oneHourHeight = (int) this.getResources().getDimension(
				R.dimen.time_gap)
				+ (int) this.getResources().getDimension(
						R.dimen.time_line_thickness);
		int height = (int) (oneHourHeight * hours);
		return new LayoutParams(LayoutParams.MATCH_PARENT, height);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		switch (view.getId()) {
		case R.id.btn_change:
			String dateAsString;

			dateAsString = String.valueOf(datePicker.getYear()) + "-"
					+ String.valueOf(datePicker.getMonth() + 1) + "-"
					+ String.valueOf(datePicker.getDayOfMonth()) + " 12:00:00";
			// "yyyy-MM-dd HH:mm:ss"
			currentDate = Utils.getISODateTime(dateAsString);

			Log.i("currentDate", Utils.getDateTimeString(currentDate));
			updateCalendarView();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainScheduleLayout = (RelativeLayout) findViewById(R.id.main_schedule_layout);
		datePicker = (DatePicker) findViewById(R.id.datePicker);

		// Set the timespace between now and after this date and before this
		// Dates before the current date
		requestHandler.setParameter("pMonateVor", "3");
		// Dates after the current date
		requestHandler.setParameter("pMonateNach", "5");

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
		this.rawResponse = rawResponse;

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
				updateCalendarView();
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

	@SuppressWarnings("deprecation")
	private void parseEvents() {
		Date dateStart;
		Date dateEnd;
		float start;
		float end;
		float hours;

		// Cursor cursor = kalMgr.getFromDbForDate(currentDate);
		Cursor cursor = kalMgr.getAllFromDb();
		while (cursor.moveToNext()) {
			final String status = cursor.getString(1);
			final String strStart = cursor.getString(5);
			final String strEnd = cursor.getString(6);

			int year = Integer.valueOf(currentDate.getYear()) + 1900;
			int month = Integer.valueOf(currentDate.getMonth()) + 1;
			int day = Integer.valueOf(currentDate.getDate());

			String requestedDateString = year + "-"
					+ String.format("%02d", month) + "-"
					+ String.format("%02d", day);

			if (strStart.contains(requestedDateString)
					&& !status.equals("CANCEL")) {
				eventView = inflateEventView();
				dateStart = Utils.getISODateTime(strStart);
				dateEnd = Utils.getISODateTime(strEnd);

				start = dateStart.getHours() * 60 + dateStart.getMinutes();
				end = dateEnd.getHours() * 60 + dateStart.getMinutes();

				hours = (end - start) / 60f;

				// Set params to eventLayout
				LayoutParams params = initLayoutParams(hours);
				setStartOfEntry(params, start / 60f);
				setText(eventView, cursor.getString(3));

				eventView.setLayoutParams(params);
				eventList.add(eventView);
			}
		}
	}

	private void setStartOfEntry(LayoutParams params, float start) {
		int oneHourHeight = (int) this.getResources().getDimension(
				R.dimen.time_one_hour);
		int marginTop = (int) (oneHourHeight * start);
		params.setMargins(0, marginTop, 0, 0);
	}

	private void setText(RelativeLayout entry, String text) {
		TextView textView = (TextView) entry.findViewById(R.id.entry_title);
		textView.setText(text);
	}

	private void updateCalendarView() {
		eventList.clear();
		parseEvents();
		mainScheduleLayout.removeAllViews();
		Log.i("Lecures found", String.valueOf(eventList.size()));
		for (RelativeLayout event : eventList) {
			mainScheduleLayout.addView(event);
		}
	}
}
