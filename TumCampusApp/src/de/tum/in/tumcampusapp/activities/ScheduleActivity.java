package de.tum.in.tumcampusapp.activities;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.LecturesHandler;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.managers.LectureItemManager;

public class ScheduleActivity extends ActivityForAccessingTumOnline {
	String fetchedMock = "<event><nr>883848212</nr><status>FT</status><url>https://campus.tum.de/tumonline/lv.detail?cLvNr=950092211</url><title>Mensch-Maschine-Kommunikation 2 VU</title><description>fix; Abhaltung; </description><dtstart>2013-06-20 09:45:00</dtstart><dtend>2013-06-20 11:15:00</dtend>0<geo><latitude>48.147119</latitude><longitude>11.566951</longitude></geo><location>1100, Hörsaal ohne exp. Bühne (0501.01.100)</location></event>";
	RelativeLayout mainScheduleLayout;
	ArrayList<RelativeLayout> scheduleList;

	public ScheduleActivity() {
		super(Const.FETCH_NOTHING, R.layout.activity_timetable_dayview);
	}

	private void checkOverlappings(ArrayList<RelativeLayout> scheduleList) {
		for (RelativeLayout entry : scheduleList) {
		}
	}

	private RelativeLayout createEntry(float start, float hours, String text) {
		RelativeLayout entry = (RelativeLayout) inflateEntry();
		LayoutParams params = initLayoutParams(hours);
		setStartOfEntry(params, start);
		setText(entry, text);
		entry.setLayoutParams(params);

		return entry;
	}

	private void getScheduleListForDay(int day) {
		// get all upcoming lecture units
		LectureItemManager lim = new LectureItemManager(this);

		// Cursor cursor = lim.getRecentFromDb();
		Cursor cursor = lim.getAllFromDb();
		cursor.moveToFirst();
		String text = cursor.getString(2);
		Log.d("Cursor", text);
	}

	private RelativeLayout inflateEntry() {
		LayoutInflater layoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return (RelativeLayout) layoutInflater.inflate(
				R.layout.layout_time_entry, null);
	}

	private LayoutParams initLayoutParams(float hours) {
		int oneHourHeight = (int) getResources().getDimension(
				R.dimen.time_one_hour);
		int height = (int) (oneHourHeight * hours);
		return new LayoutParams(LayoutParams.MATCH_PARENT, height);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mainScheduleLayout = (RelativeLayout) findViewById(R.id.main_schedule_layout);

		scheduleList = new ArrayList<RelativeLayout>();

		scheduleList.add(createEntry(1, 0.5f, "Erstes"));
		scheduleList.add(createEntry(2, 2, "Zweites"));
		scheduleList.add(createEntry(5.5f, 2, "Drittes"));
		scheduleList.add(createEntry(10, 4, "Viertes"));

		checkOverlappings(scheduleList);

		for (RelativeLayout entry : scheduleList) {
			mainScheduleLayout.addView(entry);
		}

		// TODO
		// parseEvents(fetchedMock);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onFetch(String rawResponse) {
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
			LecturesHandler lecturesHandler = new LecturesHandler();
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

	private void setStartOfEntry(LayoutParams params, float start) {
		int oneHourHeight = (int) getResources().getDimension(
				R.dimen.time_one_hour);
		int marginTop = (int) (oneHourHeight * start);
		params.setMargins(0, marginTop, 0, 0);
	}

	private void setText(RelativeLayout entry, String text) {
		TextView textView = (TextView) entry.findViewById(R.id.entry_title);
		textView.setText(text);
	}

}
