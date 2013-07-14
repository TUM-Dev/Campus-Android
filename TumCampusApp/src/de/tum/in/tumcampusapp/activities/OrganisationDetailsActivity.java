package de.tum.in.tumcampusapp.activities;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.OrgDetailsItemHandler;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.HTMLStringBuffer;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.OrgDetailsItem;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

/**
 * Show all details that are available on TUMCampus to any organisation
 * 
 * @author Thomas Behrens
 * @review Vincenz Doelle, Daniel G. Mayr
 */
@SuppressLint("DefaultLocale")
public class OrganisationDetailsActivity extends Activity implements
		TUMOnlineRequestFetchListener {

	/**
	 * Helper Class that brings the Strings+Values in a GUI polished format
	 * 
	 * @param name
	 *            Name of the Attribute
	 * @param value
	 *            Value of the Attribute
	 * @return line with name and value
	 */
	private static String makeStringShowable(String name, String value) {

		// if value has length 0 => do nothing
		if (value.length() == 0) {
			return "";
		}

		// attribute name in bold
		String outputLine = "<b>" + name + "</b>";
		// if (name + blank + value) > 36 then write value in the second line
		if (name.length() + value.length() > 35) {
			outputLine += "<br>" + value + "<br>";
		} else {
			outputLine += "\t" + value + "<br>";
		}
		return outputLine;
	}

	/**
	 * Parse XML-String into one OrgDetails-Object
	 * 
	 * @param rawResp
	 *            = XML-String to parse
	 * @return OrgDetailsItem (OrgDetails Object)
	 */
	private static OrgDetailsItem parseOrgDetails(String rawResp) {

		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory sxParserFactory = SAXParserFactory.newInstance();
		SAXParser sxParser;
		try {
			sxParser = sxParserFactory.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xmlReader = sxParser.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader */
			OrgDetailsItemHandler orgDetailsItem = new OrgDetailsItemHandler();
			xmlReader.setContentHandler(orgDetailsItem);

			/* Parse the xml-data from our URL. */
			xmlReader.parse(new InputSource(new StringReader(rawResp)));

			return orgDetailsItem.getDetails();

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("EXCEPTION", e.getMessage());
		}
		/* Parsing has finished. */
		return null;
	}

	/**
	 * Remove various signs out of a number -> Reason: To make a direct call
	 * possible
	 * 
	 * @param punctedNumber
	 *            = String can contain not numbers
	 * @return number without special characters
	 */
	private static String removePunctuation(String punctedNumber) {
		// make "(089) 56.." to "(089)56"
		punctedNumber = punctedNumber.replace(") ", ")");
		// remove whitespaces and slashes
		punctedNumber = punctedNumber.replace(" ", "-");
		punctedNumber = punctedNumber.replace("/", "-");
		// to avoid the 0 in +49(0)89
		punctedNumber = punctedNumber.replace("(0)", "");
		// to hold the number together
		punctedNumber = punctedNumber.replace("\\", "-");
		return punctedNumber;
	}

	/**
	 * Id of the organisation of which the details should be shown
	 */
	private String orgId;

	/**
	 * Only for setting it in the caption at the top
	 */
	private String orgName;

	/**
	 * To fetch the Details from the TUMCampus interface
	 */
	private TUMOnlineRequest requestHandler;

	@Override
	public void onCommonError(String errorReason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organisationdetails);

		// get the submitted (bundle) data
		Bundle bundle = this.getIntent().getExtras();
		orgId = bundle.getString(Const.ORG_ID);
		orgName = bundle.getString(Const.ORG_NAME);
	}

	/**
	 * When the data has arrived call this function, parse the Data and Update
	 * the UserInterface
	 * 
	 * @param rawResp
	 *            = XML-TUMCampus-Response (String)
	 */
	@Override
	public void onFetch(String rawResponse) {
		Log.d("RESPONSE", rawResponse);

		// parse XML into one OrgDetailsItem
		OrgDetailsItem o = parseOrgDetails(rawResponse);
		updateUI(o);
	}

	@Override
	public void onFetchCancelled() {
		// do nothing
	}

	/**
	 * while fetching a TUMOnline Request an error occured this will show the
	 * error message in a toast
	 */
	@Override
	public void onFetchError(String errorReason) {
		Utils.showLongCenteredToast(this, "Error: " + errorReason);
	}

	/**
	 * Initialize BackButton -> On Click: Go to Organisation.java and show the
	 * Organisation Tree
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// if button "back" is clicked -> make a new Bundle with the orgId and
		// start Organisation-Activity
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Bundle bundle = new Bundle();
			bundle.putString(Const.ORG_ID, orgId);
			Intent i = new Intent(OrganisationDetailsActivity.this,
					OrganisationActivity.class);
			i.putExtras(bundle);
			startActivity(i);
			return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.tvCaption);
	}

	@Override
	public void onStart() {
		super.onStart();
		// if there is a call of OrganisationDetails without an id (should not
		// be possible)
		if (orgId == null) {
			Toast.makeText(this, getString(R.string.invalid_organisation),
					Toast.LENGTH_LONG).show();
			return;
		}

		// set the name of the organisation as heading (TextView tvCaption)
		// only load the details if the details page is new and it isn't a
		// return from a link
		TextView tvCaption = (TextView) findViewById(R.id.tvCaption);
		if (tvCaption.getText().toString().compareTo(orgName) != 0) {

			// set the new organisation name in the heading
			tvCaption.setText(orgName.toUpperCase());

			// Initialise the request handler and append the orgUnitID to the
			// URL
			requestHandler = new TUMOnlineRequest("");
			requestHandler.setParameter("orgUnitID", orgId);

			// do the TUMCampus request
			requestHandler.fetchInteractive(this, this);

		}
	}

	/**
	 * Show the Organisation Details to the user
	 * 
	 * @param organisation
	 *            (= organisation detail object)
	 */
	private void updateUI(OrgDetailsItem organisation) {

		// catch error
		if (organisation == null) {
			return;
		}

		HTMLStringBuffer stringBuffer = new HTMLStringBuffer();

		TextView tvOrgDetails = (TextView) findViewById(R.id.tvOrgDetails);

		// must-have data:
		/** organisation code */
		stringBuffer.append(makeStringShowable(
				getString(R.string.abbreviation), organisation.getCode()));
		/** description */
		stringBuffer.append(makeStringShowable(getString(R.string.description),
				organisation.getDescription().replace("\n", "<br>"))); // replace
																		// \n
																		// with
																		// <br>
																		// to
																		// keep
																		// passages

		/** >>Caption - Contact Data<< */
		if ((organisation.getContactName().length() != 0)
				|| (organisation.getContactEmail().length() != 0)
				|| (organisation.getContactTelephone().length() != 0)
				|| (organisation.getContactFax().length() != 0)
				|| (organisation.getContactStreet().length() != 0)
				|| (organisation.getContactPLZ().length() != 0)
				|| (organisation.getContactLocality().length() != 0)
				|| (organisation.getContactCountry().length() != 0)) {

			stringBuffer.append("<br><u><b>"
					+ getString(R.string.contact_details) + "</b></u><br>");
		}

		/** organisation name */
		stringBuffer.append(organisation.getContactName() + "<br>");
		/** email */
		stringBuffer.append(makeStringShowable(getString(R.string.email),
				organisation.getContactEmail()));
		/** phone */
		if (organisation.getContactTelephone().length() != 0) {
			stringBuffer.append("<b>" + getString(R.string.phone) + "\t</b>");
			String tempPhoneNumber = organisation.getContactTelephone();
			tempPhoneNumber = removePunctuation(tempPhoneNumber);
			stringBuffer.append(tempPhoneNumber);
			if (organisation.getContactTelephoneType().length() != 0) {
				stringBuffer.append(" &nbsp;("
						+ organisation.getContactTelephoneType() + ") <br>");
			} else {
				stringBuffer.append(" <br>");
			}

		}
		/** fax */
		String tempFaxNumber = organisation.getContactFax();
		tempFaxNumber = removePunctuation(tempFaxNumber);
		stringBuffer.append(makeStringShowable(getString(R.string.fax),
				tempFaxNumber));
		/** street */
		stringBuffer.append(makeStringShowable(getString(R.string.street),
				organisation.getContactStreet()));
		/** plz */
		stringBuffer.append(makeStringShowable(getString(R.string.plz),
				organisation.getContactPLZ()));
		/** town */
		stringBuffer.append(makeStringShowable(getString(R.string.town),
				organisation.getContactLocality()));
		/** country */
		stringBuffer.append(makeStringShowable(getString(R.string.country),
				organisation.getContactCountry()));

		/** >>Caption - Links<< */
		if ((organisation.getContactLink().length() != 0)
				|| (organisation.getContactLocationURL().length() != 0)
				|| (organisation.getTumCampusLink().length() != 0)) {
			stringBuffer.append("<br><u><b>" + getString(R.string.links)
					+ ":</b></u><br>");
		}
		/** TUMOnline link */
		stringBuffer.append(makeStringShowable(
				getString(R.string.tumonline_link),
				organisation.getContactLink()));
		/** TUMCampus link */
		stringBuffer.append(makeStringShowable(
				getString(R.string.tumcampus_link),
				organisation.getTumCampusLink()));
		/** GoogleMaps link */
		stringBuffer.append(makeStringShowable(
				getString(R.string.googlemaps_link),
				organisation.getContactLocationURL()));

		/** Additional information */
		if (organisation.getAdditionalInfoCaption().length() != 0) {
			stringBuffer.append("<br><u><b>" + getString(R.string.add_info)
					+ ":</b></u><br>");
			// first letter of additional info in upper case
			String addInfoCaption = organisation.getAdditionalInfoCaption();
			addInfoCaption = Character.toUpperCase(addInfoCaption.charAt(0))
					+ addInfoCaption.substring(1);
			stringBuffer.append("<b>" + addInfoCaption + ":</b> "
					+ organisation.getAdditionalInfoText() + "<br>");
		}

		// show text in html
		tvOrgDetails.setText(Html.fromHtml(stringBuffer.toString()));

	}
}