package de.tum.in.tumcampusapp.auxiliary;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

public class CalendarMapper {
	private String ACCOUNT_NAME;
	private String INT_NAME_PREFIX;
	private String Calendar_Name;
	private SharedPreferences preferences;

	public CalendarMapper(String accountName, String calendarName,
			SharedPreferences preferences) {
		this.ACCOUNT_NAME = accountName;
		this.INT_NAME_PREFIX = accountName;
		this.Calendar_Name = calendarName;
		this.preferences = preferences;
	}

	public Uri addCalendar(Calendar calendar, ContentResolver cr) {
		if (calendar == null)
			throw new IllegalArgumentException();

		final ContentValues cv = buildContentValues(calendar);
		Uri calUri = buildCalUri();
		Uri cancelUri = cr.insert(calUri, cv);
		return cancelUri;
	}

	@SuppressLint("NewApi")
	private Uri buildCalUri() {
		return CalendarContract.Calendars.CONTENT_URI
				.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,
						"true")
				.appendQueryParameter(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
				.appendQueryParameter(Calendars.ACCOUNT_TYPE,
						CalendarContract.ACCOUNT_TYPE_LOCAL).build();
	}

	@SuppressLint("InlinedApi")
	private ContentValues buildContentValues(Calendar calendar) {

		int colorCalendar = 0x0066CC;

		String colorValue = preferences.getString(Const.COLOR_SCHEME, "0");
		if (colorValue != null) {
			if (colorValue.equals("0")) {
				colorCalendar = 0x0066CC;
			} else if (colorValue.equals("1")) {
				colorCalendar = 0xFF0000;
			} else if (colorValue.equals("2")) {
				colorCalendar = 0x33CC33;
			} else if (colorValue.equals("3")) {
				colorCalendar = 0x696969;

			}
		}
		String intName = INT_NAME_PREFIX + this.Calendar_Name;
		final ContentValues cv = new ContentValues();
		cv.put(Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
		cv.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
		cv.put(Calendars.NAME, intName);
		cv.put(Calendars.CALENDAR_DISPLAY_NAME, this.Calendar_Name);
		cv.put(Calendars.CALENDAR_COLOR, colorCalendar);
		cv.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
		cv.put(Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
		cv.put(Calendars.VISIBLE, 1);
		cv.put(Calendars.SYNC_EVENTS, 1);
		return cv;
	}

}
