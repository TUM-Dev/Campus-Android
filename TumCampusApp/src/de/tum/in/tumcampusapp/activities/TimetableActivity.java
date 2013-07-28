package de.tum.in.tumcampusapp.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.CalendarMapper;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.KalendarRow;
import de.tum.in.tumcampusapp.models.KalendarRowSet;
import de.tum.in.tumcampusapp.models.LecturesSearchRow;
import de.tum.in.tumcampusapp.models.LecturesSearchRowSet;
import de.tum.in.tumcampusapp.models.managers.KalendarManager;

public class TimetableActivity extends ActivityForAccessingTumOnline {
	CalendarView calendar;
	TextView timetable;
	Uri uri;

	public TimetableActivity() {
		super(Const.KALENDER, R.layout.activity_timetable);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		timetable = (TextView) findViewById(R.id.timetable);

		// Set the timespace between now and after this date and before this
		// date
		// Dates before the current date
		requestHandler.setParameter("pMonateVor", "1");
		// Dates after the current date
		requestHandler.setParameter("pMonateNach", "3");

		super.requestFetch();
	}

	@Override
	public void onFetch(String rawResponse) {
		KalendarManager kalMgr=new KalendarManager(this);
		//parsing and saving xml response
		kalMgr.importKalendar(rawResponse);
		String strKalUri = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(Const.Kalendar_Uri, "");
		//checking if calendar already exist in user's device
		if(strKalUri=="")
			addToCalendar();
		else{
			delCal(strKalUri);
			addToCalendar();
		}
		addEvents(uri);
		displayCal();
		progressLayout.setVisibility(View.GONE);
	}
	public void addToCalendar(){
		ContentResolver crv = getContentResolver();
		Calendar calendar = Calendar.getInstance();
		uri= CalendarMapper.addCalendar(calendar, crv);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = settings.edit();
		editor.putString(Const.Kalendar_Uri, uri.toString());
		editor.commit();
	}
	@SuppressWarnings("deprecation")
	public void addEvents(Uri uri){
		KalendarManager kalMgr=new KalendarManager(this);
		Date dtstart=null,dtend=null;
		
		Cursor cursor=kalMgr.getAllFromDb();
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
			if(!status.equals("CANCEL")){
				try {
					dtstart=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(strstart);
					dtend=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(strend);
					Calendar beginTime = Calendar.getInstance();
					beginTime.setTime(dtstart);
					Calendar endTime = Calendar.getInstance();
					endTime.setTime(dtend);
					
					long startMillis=beginTime.getTimeInMillis();
					long endMillis=endTime.getTimeInMillis();
					ContentResolver cr = getContentResolver();
					ContentValues values = new ContentValues();
					values.put(Events.DTSTART, startMillis);
					values.put(Events.DTEND, endMillis);
					values.put(Events.TITLE, title);
					values.put(Events.DESCRIPTION, description);
					values.put(Events.CALENDAR_ID,getID(uri));
					values.put(Events.EVENT_TIMEZONE, "America/Los_Angeles");
					Uri uriInsert = cr.insert(Events.CONTENT_URI, values);

				}
				catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	

	}
	//get added calendar id
	public String getID(Uri uri){
		String[] projection = new String[] { "_id", "name" };
		ContentResolver ctnresolver = this.getContentResolver();
		Cursor cursor = ctnresolver.query(uri, projection, null, null, null);
		String idstring="0";
		while (cursor.moveToNext()) {
			idstring = cursor.getString(0);
		}
		return idstring;
	}
	//displaying calendar
	public void displayCal(){
		//displaying Calendar
		Calendar beginTime = Calendar.getInstance();
		long startMillis = beginTime.getTimeInMillis();
		Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
		builder.appendPath("time");
		ContentUris.appendId(builder, startMillis);
		Intent intent = new Intent(Intent.ACTION_VIEW)
		.setData(builder.build());
		startActivity(intent);	
	}

	public void delCal(String strKalUri){
		Uri kalUri=Uri.parse(strKalUri);
        ContentResolver crv = getContentResolver();
        crv.delete(kalUri, null,null);
   }

}
