package de.tum.in.tumcampus.adapters;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import de.tum.in.tumcampus.models.OrgDetailsItem;

/**
 * Class that handles an OrgDetailsObject and SAX-Parses the XML containing such
 * Objects The parsed Object has no child Objects (but can be implemented also
 * with child Objects)
 * 
 * @author Thomas Behrens
 * @review Vincenz Doelle, Daniel G. Mayr
 */

public class OrgDetailsItemHandler extends DefaultHandler {

	// TODO NOT STABLE!

	public static final String TAG_ADDRESSE_TEXT = "adresse_text";
	public static final String TAG_BIB = "bibliothek_info";
	public static final String TAG_CONTACT_NAME = "ansprechpartner";
	public static final String TAG_EXTRA = "zusatz:info";
	public static final String TAG_EXTRA_NAME = "zusatz_info_name";
	public static final String TAG_FAX = "fax_nummer";
	public static final String TAG_HOMEPAGE = "www_homepage";
	public static final String TAG_KENNUNG = "kennung";
	public static final String TAG_MAIL = "email_adresse";
	public static final String TAG_NAME = "name";
	public static final String TAG_NR = "nr";
	public static final String TAG_SEKRETARIAT = "sekretariat_info";
	public static final String TAG_TEL = "telefon_nummer";

	// Buffer for parsing
	StringBuffer buff;

	boolean buffering = false;
	// save if the parser is inside Additional Information
	// to handle those another way
	boolean isInsideAdditionalInformation = false;

	// OrganisationDetails Object to load parsed data into
	private final OrgDetailsItem odo = new OrgDetailsItem();

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
		if (localName.equals(TAG_NR) || localName.equals(TAG_NAME)
				|| localName.equals(TAG_KENNUNG)
				|| localName.equals(TAG_CONTACT_NAME)
				|| localName.equals(TAG_ADDRESSE_TEXT)
				|| localName.equals(TAG_SEKRETARIAT)
				|| localName.equals(TAG_TEL) || localName.equals(TAG_FAX)
				|| localName.equals(TAG_MAIL) || localName.equals(TAG_HOMEPAGE)) {
			buffering = false;

			// String-Switch:
			// Set attributes depending on localname of the tag
			if (localName.equals(TAG_NR)) {
				odo.setId(buff.toString());
			} else if (localName.equals(TAG_NAME)) {
				odo.setName(buff.toString());
			} else if (localName.equals(TAG_KENNUNG)) {
				odo.setCode(buff.toString());
			} else if (localName.equals(TAG_CONTACT_NAME)) {
				odo.setContactName(buff.toString());
			} else if (localName.equals(TAG_ADDRESSE_TEXT)) {
				odo.setContactStreet(buff.toString());
			} else if (localName.equals(TAG_SEKRETARIAT)) {
				odo.setContactLocality(buff.toString());
			} else if (localName.equals(TAG_TEL)) {
				odo.setContactTelephone(buff.toString());
			} else if (localName.equals(TAG_FAX)) {
				odo.setContactFax(buff.toString());
			} else if (localName.equals(TAG_MAIL)) {
                String mail = buff.toString();
                mail = mail.replace("ä","ae");
                mail = mail.replace("ö","oe");
                mail = mail.replace("ü","ue");
				odo.setContactEmail(mail);
			} else if (localName.equals(TAG_HOMEPAGE)) {
				odo.setContactLocationURL(buff.toString());
			} else if (localName.equals(TAG_EXTRA_NAME)) {
				odo.setAdditionalInfoCaption(buff.toString());
			} else if (localName.equals(TAG_EXTRA)) {
				odo.setAdditionalInfoText(buff.toString());
			} else if (localName.equals(TAG_BIB)) {
				odo.setContactAdditionalInfo(buff.toString());
			}
		}
	}

	/**
	 * Returns the collected Organisation Details to the Calling Class
	 * 
	 * @return OrgDetails Object
	 */
	public OrgDetailsItem getDetails() {
		return odo;
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
		if (localName.equals(TAG_NR) || localName.equals(TAG_NAME)
				|| localName.equals(TAG_KENNUNG)
				|| localName.equals(TAG_CONTACT_NAME)
				|| localName.equals(TAG_ADDRESSE_TEXT)
				|| localName.equals(TAG_SEKRETARIAT)
				|| localName.equals(TAG_TEL) || localName.equals(TAG_FAX)
				|| localName.equals(TAG_MAIL) || localName.equals(TAG_HOMEPAGE)) {
			buff = new StringBuffer("");
			buffering = true;
		}
	}
}
