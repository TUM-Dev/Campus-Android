package de.tum.in.tumcampusapp.auxiliary;

import java.util.Calendar;

import android.R.color;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

public class CalendarMapper {
	private static final String ACCOUNT_NAME = "TUM_Campus_APP";
	private static final String INT_NAME_PREFIX = "TUM_Campus_APP";

	@SuppressWarnings("deprecation")
	public static Uri addCalendar(Calendar calendar, ContentResolver cr) {
		if (calendar == null)
			throw new IllegalArgumentException();

		final ContentValues cv = buildContentValues(calendar);
		Uri calUri = buildCalUri();
		Uri cancelUri = cr.insert(calUri, cv);
		return cancelUri;
	}

	@SuppressLint("NewApi")
	private static Uri buildCalUri() {
		return CalendarContract.Calendars.CONTENT_URI
				.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,
						"true")
				.appendQueryParameter(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
				.appendQueryParameter(Calendars.ACCOUNT_TYPE,
						CalendarContract.ACCOUNT_TYPE_LOCAL).build();
	}

	private static ContentValues buildContentValues(Calendar calendar) {
		String dispName = "TUM Campus";
		String intName = INT_NAME_PREFIX + dispName;
		final ContentValues cv = new ContentValues();
		cv.put(Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
		cv.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
		cv.put(Calendars.NAME, intName);
		cv.put(Calendars.CALENDAR_DISPLAY_NAME, dispName);
		cv.put(Calendars.CALENDAR_COLOR, color.holo_orange_dark);
		cv.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
		cv.put(Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
		cv.put(Calendars.VISIBLE, 1);
		cv.put(Calendars.SYNC_EVENTS, 1);
		return cv;
	}

}
