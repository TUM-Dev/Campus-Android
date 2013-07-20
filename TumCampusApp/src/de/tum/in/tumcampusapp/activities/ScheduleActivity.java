package de.tum.in.tumcampusapp.activities;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.LecturesHandler;
import de.tum.in.tumcampusapp.auxiliary.Const;

public class ScheduleActivity extends ActivityForAccessingTumOnline {
	String fetchedMock = "<events>"
			+ "<event><nr>883815573</nr><status>CANCEL</status>"
			+ "<url>https://campus.tum.de/tumonline/lv.detail?cLvNr=950091407</url>"
			+ "<title>Praktikum - Betriebssysteme - Google Android (IN0012, IN2106, IN4004) PR</title>"
			+ "<description>gelöscht; Abhaltung; </description>"
			+ "<dtstart>2013-06-20 8:00:00</dtstart>"
			+ "<dtend>2013-06-20 12:00:00</dtend>"
			+ "<location>0.01.05, Seminarraum 2 (8102.EG.105)</location>"
			+ "</event>"
			+ "<event><nr>883815573</nr><status>CANCEL</status>"
			+ "<url>https://campus.tum.de/tumonline/lv.detail?cLvNr=950091407</url>"
			+ "<title>Praktikum - Betriebssysteme - Google Android (IN0012, IN2106, IN4004) PR</title>"
			+ "<description>gelöscht; Abhaltung; </description>"
			+ "<dtstart>2013-06-20 16:00:00</dtstart>"
			+ "<dtend>2013-06-20 17:30:00</dtend>"
			+ "<location>0.01.05, Seminarraum 2 (8102.EG.105)</location>"
			+ "</event></events>";
	RelativeLayout mainScheduleLayout;
	ArrayList<RelativeLayout> scheduleList;

	public ScheduleActivity() {
		super(Const.KALENDER, R.layout.activity_timetable_dayview);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainScheduleLayout = (RelativeLayout) findViewById(R.id.main_schedule_layout);
	}
	
	private void updateCalendarWithXML(String fetchedXml) {
		scheduleList = parseEvents(fetchedXml);
		for (RelativeLayout entry : scheduleList) {
			mainScheduleLayout.addView(entry);
		}
	}

	@Override
	public void onFetch(String rawResponse) {
		updateCalendarWithXML(rawResponse);
		progressLayout.setVisibility(View.GONE);
	}

	private ArrayList<RelativeLayout> parseEvents(String rawResp) {

		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory sxParserFactory = SAXParserFactory.newInstance();
		SAXParser sxParser;
		try {
			sxParser = sxParserFactory.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xmlReader = sxParser.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader */
			LecturesHandler lecturesHandler = new LecturesHandler(this);
			xmlReader.setContentHandler(lecturesHandler);

			/* Parse the xml-data from our URL. */
			xmlReader.parse(new InputSource(new StringReader(rawResp)));

			return lecturesHandler.getLectureList();

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("EXCEPTION", e.getMessage());
		}
		/* Parsing has finished. */
		return null;
	}
}
