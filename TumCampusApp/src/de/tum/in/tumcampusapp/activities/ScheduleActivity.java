package de.tum.in.tumcampusapp.activities;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.LecturesHandler;
import de.tum.in.tumcampusapp.auxiliary.Const;

public class ScheduleActivity extends ActivityForAccessingTumOnline {
	RelativeLayout mainScheduleLayout;
	ArrayList<RelativeLayout> scheduleList;
	String rawResponse;
	Date currentDate = new Date();
	DatePicker datePicker;

	public ScheduleActivity() {
		super(Const.KALENDER, R.layout.activity_timetable_dayview);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainScheduleLayout = (RelativeLayout) findViewById(R.id.main_schedule_layout);
		datePicker = (DatePicker) findViewById(R.id.datePicker);
	}

	private void updateCalendarWithXML(Date date) {
		if (this.rawResponse != null) {
			mainScheduleLayout.removeAllViews();
			scheduleList = parseEvents(date);
			Log.i("found", String.valueOf(scheduleList.size()));
			for (RelativeLayout entry : scheduleList) {
				mainScheduleLayout.addView(entry);
			}
		} else {
			Toast.makeText(this, "Please fetch first", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onClick(View view) {
		super.onClick(view);
		switch (view.getId()) {
		case R.id.btn_change:
			// currentDate.setDate(currentDate.getDate() - 1);
			currentDate = new Date(datePicker.getYear(),datePicker.getMonth() + 1,datePicker.getDayOfMonth());
			Log.i("currentDate", currentDate.toGMTString());
			updateCalendarWithXML(currentDate);
			break;
		}
	}

	@Override
	public void onFetch(String rawResponse) {
		this.rawResponse = rawResponse;
		updateCalendarWithXML(currentDate);
		progressLayout.setVisibility(View.GONE);
	}
	
	private ArrayList<RelativeLayout> parseEvents(Date date) {

		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory sxParserFactory = SAXParserFactory.newInstance();
		SAXParser sxParser;
		try {
			sxParser = sxParserFactory.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xmlReader = sxParser.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader */
			LecturesHandler lecturesHandler = new LecturesHandler(this);

			Date dateFixed = new Date(date.getYear()-1900, date.getMonth()-1, date.getDate());
			Log.i("dateFixed", dateFixed.toGMTString());

			lecturesHandler
					.setRequestedDate(dateFixed);

			xmlReader.setContentHandler(lecturesHandler);

			/* Parse the xml-data from our URL. */
			xmlReader.parse(new InputSource(new StringReader(rawResponse)));

			return lecturesHandler.getLectureList();

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("EXCEPTION", e.getMessage());
		}
		/* Parsing has finished. */
		return null;
	}
}
