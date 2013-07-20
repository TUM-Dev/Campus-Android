package de.tum.in.tumcampusapp.adapters;

import java.util.ArrayList;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Class that handles an OrgDetailsObject and SAX-Parses the XML containing such
 * Objects The parsed Object has no child Objects (but can be implemented also
 * with child Objects)
 * 
 * @author Thomas Behrens
 * @review Vincenz Doelle, Daniel G. Mayr
 */

public class LecturesHandler extends DefaultHandler {

	public static final String TAG_EVENT = "event";
	public static final String TAG_TITLE = "title";
	public static final String TAG_START = "dtstart";
	public static final String TAG_END = "dtend";
	public static final String TAG_LOCATION = "location";

	// Buffer for parsing
	StringBuffer buff;
	private Context context;

	private float hours;
	private float start;
	private float end;
	private Date date;

	private RelativeLayout lecture;
	// OrganisationDetails Object to load parsed data into
	private ArrayList<RelativeLayout> lectureList = new ArrayList<RelativeLayout>();

	private RelativeLayout inflateEntry() {
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return (RelativeLayout) layoutInflater.inflate(
				R.layout.layout_time_entry, null);
	}

	private LayoutParams initLayoutParams(float hours) {
		int oneHourHeight = (int) context.getResources().getDimension(
				R.dimen.time_one_hour);
		int height = (int) (oneHourHeight * hours);
		return new LayoutParams(LayoutParams.MATCH_PARENT, height);
	}

	private void setStartOfEntry(LayoutParams params, float start) {
		int oneHourHeight = (int) context.getResources().getDimension(
				R.dimen.time_one_hour);
		int marginTop = (int) (oneHourHeight * start);
		params.setMargins(0, marginTop, 0, 0);
	}

	private void setText(RelativeLayout entry, String text) {
		TextView textView = (TextView) entry.findViewById(R.id.entry_title);
		textView.setText(text);
	}
	
	private void appendText(RelativeLayout entry, String text) {
		TextView textView = (TextView) entry.findViewById(R.id.entry_title);
		textView.append(text);
	}

	public LecturesHandler(Context context) {
		this.context = context;
	}

	@Override
	public void characters(char ch[], int start, int length) {
		buff.append(ch, start, length);
	}

	@Override
	public void endDocument() {
		Log.d("sax-parser", "end sax-parsing XML-document");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void endElement(String namespaceURI, String localName, String qName) {

		if (localName.equals(TAG_EVENT)) {
			// Set params to eventLayout
			LayoutParams params = initLayoutParams(hours);
			setStartOfEntry(params, start / 60f);
			lecture.setLayoutParams(params);

			Log.d("Date", "getDay " + date.getDay());
			Log.d("Date", "getDate " + date.getDate());
			Log.d("Date", "getMonth " + date.getMonth());
			Log.d("Date", "getYear " + date.getYear());
			Log.d("Date", "new Date() " + new Date().toGMTString());
			
			// Add event layout to list
			if (date.getDate() == 20 && date.getMonth() == 5 && date.getYear() == 113) {
				lectureList.add(lecture);
			}
		}
		if (localName.equals(TAG_TITLE)) {
			setText(lecture, buff.toString());
		}
		if (localName.equals(TAG_START)) {
			date = Utils.getISODateTime(buff.toString());
			start = date.getHours() * 60 + date.getMinutes();
		}
		if (localName.equals(TAG_END)) {
			date = Utils.getISODateTime(buff.toString());
			end = date.getHours() * 60 + date.getMinutes();
			hours = (end - start) / 60f;
		}
		if (localName.equals(TAG_LOCATION)) {
			appendText(lecture, "\n" + buff.toString());
		}
	}

	public ArrayList<RelativeLayout> getLectureList() {
		return lectureList;
	}

	@Override
	public void startDocument() {
		Log.d("sax-parser", "start sax-parsing XML-document");
	}

	// TODO Check whether refactor list of interesting tags
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) {
		buff = new StringBuffer("");
		if (localName.equals(TAG_EVENT)) {
			lecture = inflateEntry();
		}
	}
}
