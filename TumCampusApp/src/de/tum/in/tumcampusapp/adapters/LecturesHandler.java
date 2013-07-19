package de.tum.in.tumcampusapp.adapters;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;

/**
 * Class that handles an OrgDetailsObject and SAX-Parses the XML containing such
 * Objects The parsed Object has no child Objects (but can be implemented also
 * with child Objects)
 * 
 * @author Thomas Behrens
 * @review Vincenz Doelle, Daniel G. Mayr
 */

public class LecturesHandler extends DefaultHandler {

	public static final String TAG_TITLE = "title";

	// Buffer for parsing
	StringBuffer buff;
	boolean buffering = false;

	private final RelativeLayout lecture = new RelativeLayout(null);
	// OrganisationDetails Object to load parsed data into
	private final ArrayList<RelativeLayout> lectureList = new ArrayList<RelativeLayout>();

	@Override
	public void characters(char ch[], int start, int length) {
		if (buffering) {
			buff.append(ch, start, length);
		}
	}

	@Override
	public void endDocument() {
		Log.d("sax-parser", "end sax-parsing XML-document");
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) {
		// end buffer of interesting tags to handle their content
		if (localName.equals(TAG_TITLE)) {
			buffering = false;

			// String-Switch:
			// Set attributes depending on localname of the tag
			if (localName.equals(TAG_TITLE)) {
				((TextView) lecture.findViewById(R.id.entry_title))
						.setText(buff.toString());
			}
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
		// only buffer interesting tags
		if (localName.equals(TAG_TITLE)) {
			buff = new StringBuffer("");
			buffering = true;
		}
	}
}
