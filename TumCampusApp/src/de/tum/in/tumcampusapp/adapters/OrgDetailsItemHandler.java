package de.tum.in.tumcampusapp.adapters;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.OrgDetailsItem;

/**
 * Class that handles an OrgDetailsObject and SAX-Parses the XML containing such
 * Objects The parsed Object has no child Objects (but can be implemented also
 * with child Objects)
 * 
 * @author Thomas Behrens
 * @review Vincenz Doelle, Daniel G. Mayr
 */

public class OrgDetailsItemHandler extends DefaultHandler {

	// OrganisationDetails Object to load parsed data into
	private final OrgDetailsItem odo = new OrgDetailsItem();

	// Buffer for parsing
	StringBuffer buff;
	boolean buffering = false;

	// stores temporarily the attribute of the tag,
	// to have access to it at the end tag
	public String tempAtt;

	// save if the parser is inside Additional Information
	// to handle those another way
	boolean isInsideAdditionalInformation = false;

	// TODO Check whether refactor list of interesting tags
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) {
		// only buffer interesting tags
		if (localName.equals("orgUnitID") || localName.equals("orgUnitName")
				|| localName.equals("orgUnitCode")
				|| localName.equals("orgUnitDescription")
				|| localName.equals("contactName")
				|| localName.equals("street") || localName.equals("locality")
				|| localName.equals("pcode") || localName.equals("country")
				|| localName.equals("telephone") || localName.equals("fax")
				|| localName.equals("email") || localName.equals("webLink")
				|| localName.equals("subBlock")) {
			buff = new StringBuffer("");
			buffering = true;
			// to store first attribute till end tag
			tempAtt = atts.getValue(0);
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) {
		// end buffer of interesting tags to handle their content
		if (localName.equals("orgUnitID") || localName.equals("orgUnitName")
				|| localName.equals("orgUnitCode")
				|| localName.equals("orgUnitDescription")
				|| localName.equals("contactName")
				|| localName.equals("street") || localName.equals("locality")
				|| localName.equals("pcode") || localName.equals("country")
				|| localName.equals("telephone") || localName.equals("fax")
				|| localName.equals("email") || localName.equals("webLink")
				|| localName.equals("subBlock")) {
			buffering = false;

			// String-Switch:
			// Set attributes depending on localname of the tag
			if (localName.equals("orgUnitID")) {
				odo.setId(buff.toString());
			} else if (localName.equals("orgUnitName")) {
				// additionally cut <text> and </text>
				odo.setName(Utils.cutText(buff.toString(), "<text>", "</text>"));
			} else if (localName.equals("orgUnitCode")) {
				odo.setCode(buff.toString());
			} else if (localName.equals("orgUnitDescription")) {
				odo.setDescription(buff.toString());
			} else if (localName.equals("contactName")) {
				// additionally cut <text> and </text>
				odo.setContactName(Utils.cutText(buff.toString(), "<text>",
						"</text>"));
			} else if (localName.equals("street")) {
				odo.setContactStreet(buff.toString());
			} else if (localName.equals("locality")) {
				odo.setContactLocality(buff.toString());
			} else if (localName.equals("pcode")) {
				odo.setContactPLZ(buff.toString());
			} else if (localName.equals("country")) {
				odo.setContactCountry(buff.toString());
			} else if (localName.equals("telephone")) {
				odo.setContactTelephone(buff.toString());
				odo.setContactTelephoneType(tempAtt);
			} else if (localName.equals("fax")) {
				odo.setContactFax(buff.toString());
			} else if (localName.equals("email")) {
				odo.setContactEmail(buff.toString());
			} else if (localName.equals("webLink")) {
				// to differ the different links using tempAtt
				if (tempAtt == null) {
					tempAtt = "null";
				}
				if (tempAtt.compareTo("locationURL") == 0) {
					// location link
					odo.setContactLocationURL(buff.toString());
				} else if (tempAtt.compareTo("CAMPUSonlineURL") == 0) {
					// TUMCampus link
					odo.setTumCampusLink(buff.toString());
				} else if (tempAtt.compareTo("null") == 0) {
					// TUMOnline link (or other Website)
					odo.setContactLink(buff.toString());
				} else {
					// error: other link
					Log.d("ERROR:", "other link: " + tempAtt);
				}
			} else if (localName.equals("subBlock")) {
				// to handle the sometimes recursive structure
				if (isInsideAdditionalInformation == true) {
					odo.setAdditionalInfoCaption(tempAtt);
					int offset = odo.getAdditionalInfoCaption().length();
					odo.setAdditionalInfoText(buff.toString().substring(
							25 + offset, buff.toString().length() - 11));
					isInsideAdditionalInformation = false;
				} else if (tempAtt == null) {
					return;
				}
				// if the block contains additional Information
				if (tempAtt.compareTo("additionalInformation") == 0) {
					isInsideAdditionalInformation = true;
				} else {
					odo.setAdditionalInfoCaption(tempAtt);
					odo.setAdditionalInfoText(buff.toString());
				}
			}
		}
	}

	@Override
	public void startDocument() {
		Log.d("sax-parser", "start sax-parsing XML-document");
	}

	@Override
	public void endDocument() {
		Log.d("sax-parser", "end sax-parsing XML-document");
	}

	@Override
	public void characters(char ch[], int start, int length) {
		if (buffering) {
			buff.append(ch, start, length);
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

}
