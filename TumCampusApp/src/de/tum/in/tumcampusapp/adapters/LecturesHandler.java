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

	public static final String TAG_DESCRIPTION = "description";
	public static final String TAG_END = "dtend";
	public static final String TAG_EVENT = "event";
	public static final String TAG_LOCATION = "location";
	public static final String TAG_START = "dtstart";
	public static final String TAG_STATUS = "status";
	public static final String TAG_TITLE = "title";

	// Buffer for parsing
	StringBuffer buff;
	private Context context;
	private Date date;

	private float end;
	private float hours;
	private boolean isEventDeletedOrPostponed;
	private RelativeLayout lecture;
	// OrganisationDetails Object to load parsed data into
	private ArrayList<RelativeLayout> lectureList = new ArrayList<RelativeLayout>();

	private Date requestedDate;
	private float start;

	public LecturesHandler(Context context) {
		this.context = context;
	}

	private void appendText(RelativeLayout entry, String text) {
		TextView textView = (TextView) entry.findViewById(R.id.entry_title);
		textView.append(text);
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
			if (isEventDeletedOrPostponed) {
				return;
			}
			// Set params to eventLayout
			LayoutParams params = initLayoutParams(hours);
			setStartOfEntry(params, start / 60f);
			lecture.setLayoutParams(params);

			Log.d("event-date", date.toGMTString());
			Log.d("requested-date", requestedDate.toGMTString());

			// Add event layout to list
			if (date.getDate() == requestedDate.getDate()
					&& date.getMonth() == requestedDate.getMonth()
					&& date.getYear() == requestedDate.getYear()) {
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
		}
		if (localName.equals(TAG_DESCRIPTION)) {
		}
		if (localName.equals(TAG_STATUS)) {
			if (buff.toString().contains("CANCEL")) {
				isEventDeletedOrPostponed = true;
			} else {
				isEventDeletedOrPostponed = false;
			}
		}
	}

	public ArrayList<RelativeLayout> getLectureList() {
		return lectureList;
	}

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

	public void setRequestedDate(Date requestedDate) {
		this.requestedDate = requestedDate;
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
