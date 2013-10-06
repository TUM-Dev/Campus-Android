package de.tum.in.tumcampusapp.activities;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.OrgItemListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.FileUtils;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.OrgItem;
import de.tum.in.tumcampusapp.models.OrgItemList;

/**
 * Activity that shows the first level of organisations at TUM.
 * 
 * @author Thomas Behrens
 * @review Vincenz Doelle, Daniel G. Mayr, Sascha Moecker
 */
@SuppressLint("DefaultLocale")
public class OrganisationActivity extends ActivityForAccessingTumOnline
		implements OnClickListener {

	/**
	 * language is "de"->German or "en"->English depending on the system
	 * language
	 */
	private static String language;

	/**
	 * To show at start the highest Organisation level (The highest
	 * Organisations are child of "Organisation 1" = TUM)
	 */
	private static final String TOP_LEVEL_ORG = "1";

	private OrganisationActivity activity = this;

	/** The document is used to access and parse the xml.org file on the SD-card */
	private Document doc;

	protected RelativeLayout errorLayout;

	/** List of Organisations shown on the Display */
	private ListView lvOrg;

	/** orgId is the ID of the organisation you click on */
	private String orgId;

	/** orgName is the name of the parent organisation, whose folder is showed */
	private String orgName;

	/**
	 * parentId is the ID of the parent organisation, of which the
	 * sub-organisations are showed
	 */
	private String parentId;

	protected RelativeLayout progressLayout;
	/** The org.xml File on the SD-card */
	private File xmlOrgFile;

	public OrganisationActivity() {
		super(Const.ORG_TREE, R.layout.activity_organisation);
	}

	/**
	 * SAX-Parsing the org.xml-File to get Information for the Jump in the
	 * Organisation Structure
	 */
	private void buildDocument() {
		// (sax) dom parsing
		DocumentBuilderFactory docBuilderFactory;
		DocumentBuilder docBuilder;
		doc = null;
		try {
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse(getOrgFile());
		} catch (Exception e) {
			Log.d("EXCEPTION", e.getMessage());
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
	}

	/**
	 * Returns true if there are one or more elements in the organisation tree
	 * inside this organisation
	 * 
	 * @param organisationId
	 * @return
	 */
	private boolean existSuborganisation(String organisationId) {

		// get list of all organisations
		NodeList organisationList = doc.getElementsByTagName("row");
		Log.d("PARSING", "parsing " + organisationList.getLength()
				+ " elements...");

		// go through each organisation
		for (int i = 0; i < organisationList.getLength(); i++) {

			// extract one organisation to an element
			Node organisationItem = organisationList.item(i);
			// get the parentId of the element
			String itemParentId = getValue(organisationItem, "parent");

			// if there is any item with the parentId of the id return true -->
			// there is at least one suborganisation
			// existing
			if (itemParentId.contentEquals(organisationId)) {
				return true;
			}
		}
		return false;
	}

	private File getOrgFile() {

		if (xmlOrgFile == null) {
			// File linking to SD-card to a xml, that contains the whole
			// organisation-tree
			try {
				xmlOrgFile = FileUtils.getFileOnSD(Const.ORGANISATIONS,
						"org.xml");
				super.hideProgressLayout();
				super.hideErrorLayout();
			} catch (Exception e) {
				Toast.makeText(this, R.string.exception_sdcard,
						Toast.LENGTH_SHORT).show();
				super.hideProgressLayout();
				super.showErrorLayout();
				return null;
			}

			// check if XML file exists and if it is bigger than 100kB (it is
			// approximately 317kb, if the import isn't
			// "wrong token")
			// if no valid XML file -> set Token, Download XML data and start
			// 'Organisations' again
			if (!xmlOrgFile.exists() || !xmlOrgFile.isFile()
					|| !(xmlOrgFile.length() > 100000)) {

				// Request to fetch via TUMRequestManager
				super.requestFetch();

				// call this function recursive, so it should not be null and
				// return the file
				return null;
			}
		}
		return xmlOrgFile;
	}

	/**
	 * Searches for the parentId of an element, if it is already in the highest
	 * layer, it returns 1.
	 * 
	 * @param parentId
	 * @return
	 */
	public OrgItem getParent(String parentId) {

		// if already in highest layer => create OrgItem of highest layer
		if (parentId.equals(TOP_LEVEL_ORG)) {
			OrgItem topObject = new OrgItem();
			topObject.setId(TOP_LEVEL_ORG);
			return topObject;
		}

		// get all elements to parse and count them
		NodeList organisationList = doc.getElementsByTagName("row");
		Log.d("PARSING", "parsing " + organisationList.getLength()
				+ " elements...");

		// parse xml tree (org.xml) to find parent of an element
		for (int i = 0; i < organisationList.getLength(); i++) {
			Node organisationItem = organisationList.item(i);

			// go through every id and look if it there is any equal in any
			// parent-Id field
			String itemId = getValue(organisationItem, "nr");

			// if there is an organisation that has the given parentId as
			// organisationId
			// make a parent element and return it
			if (itemId.equals(parentId)) {

				// set the Name depending on the system language
				String itemName;
				if (language.equals("de")) {
					itemName = getValue(organisationItem, "name_de");
				} else {
					itemName = getValue(organisationItem, "name_en");
				}

				// get the parentId of the Item
				String itemParentId = getValue(organisationItem, "parent");

				// create new Organisation (OrgItem) and instantiate it
				// with the data of the found parent Object
				OrgItem parentObject = new OrgItem();
				parentObject.setId(itemParentId);
				if (language.equals(Const.DE)) {
					parentObject.setNameDe(itemName);
				} else {
					parentObject.setNameEn(itemName);
				}
				return parentObject;
			}
		}
		// if no parent found => jump to start layer
		OrgItem parentObject = new OrgItem();
		parentObject.setId(TOP_LEVEL_ORG);
		if (language.equals(Const.DE)) {
			parentObject.setNameDe(getString(R.string.tum));
		} else {
			parentObject.setNameEn(getString(R.string.tum));
		}
		return parentObject;
	}

	/**
	 * Function that gets the Value out of a Node with a special name
	 * 
	 * @param item
	 *            = Node that gets evaluated
	 * @param type
	 *            = Type of node (e.g. parent, id, nameDe)
	 */
	public String getValue(Node item, String type) {
		Element elem = (Element) item;
		// filter the item with a special tag
		NodeList list = elem.getElementsByTagName(type);
		// take first list element (list only has one element)
		Element elem2 = (Element) list.item(0);
		// now get the value out of the childnode nr. 1
		list = elem2.getChildNodes();
		return list.item(0).getNodeValue();
	}

	/**
	 * A click on the BackButton should show the parent class or go back to the
	 * main menu
	 */
	@Override
	public void onBackPressed() {
		// go back to the main menu, if the user is in the highest level
		if (orgId.equals(TOP_LEVEL_ORG)) {
			Intent intent = new Intent(this, StartActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return;
		}

		// get one layer up
		orgId = parentId;
		OrgItem p = getParent(orgId);
		parentId = p.getId();

		// Switch language
		// -> German if German is system language
		// if not German -> English
		if (language.equals(Const.DE)) {
			orgName = getParent(parentId).getNameDe();
		} else {
			orgName = getParent(parentId).getNameEn();
		}
		showItems(parentId);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// list of organizations
		lvOrg = (ListView) findViewById(R.id.lstOrganisations);

		// start at the top level
		this.orgId = TOP_LEVEL_ORG;
		this.parentId = TOP_LEVEL_ORG;
		// TODO Check for rightful checking. Check in the whole class.
		// set language = German if system language is German else set English
		if (System.getProperty("user.language").compareTo(Const.DE) == 0) {
			language = Const.DE;
		} else {
			language = Const.EN;
		}

		// get the XML file containing all organisations information
		if (getOrgFile() != null) {
			showOrgTree();
		}
	}

	@Override
	public void onFetch(String rawResponse) {

		try {
			Utils.getCacheDir("organisations");
			FileUtils.writeFile(xmlOrgFile, rawResponse);
			Log.d("Import: org.xml",
					"Xml file has been new downloaded and saved.");
			super.hideErrorLayout();
			super.hideProgressLayout();
			showOrgTree();

		} catch (IOException e) {
			Toast.makeText(this, R.string.exception_sdcard, Toast.LENGTH_SHORT)
					.show();
			super.hideProgressLayout();
			super.showErrorLayout();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.tvCaption);
	}

	/**
	 * Show all items in a certain layer having a parent element with parent_id
	 * = parent.
	 * 
	 * @param parent
	 *            all items with the same parent
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void showItems(String parent) {

		// caption button gets caption
		TextView tvCaption = (TextView) findViewById(R.id.tvCaption);

		// if no orgName -> highest level
		if (orgName == null) {
			orgName = getString(R.string.tum);
		}

		// set caption (organisation "folder" name)
		tvCaption.setText(orgName.toUpperCase());

		NodeList nodeList = doc.getElementsByTagName("row");
		Log.d("PARSING", "parsing " + nodeList.getLength() + " elements...");

		OrgItemList organisationList = new OrgItemList();

		// go through the XML file and give each organisation its Id, German
		// name, English name and parent-Id
		for (int i = 0; i < nodeList.getLength(); i++) {

			Node node = nodeList.item(i);
			OrgItem oItem = new OrgItem();

			// get the parent ID of the current item
			String itemParentId = getValue(node, "parent");

			// is this element one we are searching for? (has the parentId of
			// the clicked Element)
			if (itemParentId.contentEquals(parent)) {

				// get the value of the name_de, name_en and nr element and save
				// them in the current oItem
				oItem.setId(getValue(node, "nr"));
				oItem.setNameDe(getValue(node, "name_de"));
				oItem.setNameEn(getValue(node, "name_en"));
				oItem.setParentId(itemParentId);

				// add this organisation item to the organisation list
				organisationList.add(oItem);
			}
		}

		lvOrg.setAdapter(new OrgItemListAdapter(OrganisationActivity.this,
				organisationList.getGroups()));

		// action for clicks on a list-item
		lvOrg.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				Object o = lvOrg.getItemAtPosition(position);
				OrgItem org = (OrgItem) o;

				// look if no suborganisation exists, and if not make bundle and
				// start OrganisationDetails
				if (!existSuborganisation(org.getId())) {
					Bundle bundle = new Bundle();
					bundle.putString(Const.ORG_PARENT_ID, org.getParentId());
					bundle.putString(Const.ORG_ID, org.getId());

					// set orgName depending on language
					if (language.equals(Const.DE)) {
						bundle.putString(Const.ORG_NAME, org.getNameDe());
					} else {
						bundle.putString(Const.ORG_NAME, org.getNameEn());
					}

					// show organisation details
					Intent i = new Intent(OrganisationActivity.this,
							OrganisationDetailsActivity.class);
					i.putExtras(bundle);
					startActivity(i);

				} else {
					// if suborganisation exists, show suborganisation structure
					parentId = orgId;
					orgId = org.getId();
					// switch correct language
					if (language.equals(Const.DE)) {
						orgName = org.getNameDe();
					} else {
						orgName = org.getNameEn();
					}
					showItems(orgId);
				}
			}
		});
	}

	private void showOrgTree() {

		AsyncTask<Void, Void, Boolean> backgroundTask;
		backgroundTask = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				// be careful! this takes a lot of time on older devices!
				buildDocument();

				// set orgName depending on language
				if (language.equals(Const.DE)) {
					orgName = getParent(parentId).getNameDe();
				} else {
					orgName = getParent(parentId).getNameEn();
				}
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				// first: show the first level of the tree (including the
				// faculties)
				showItems(parentId);
				activity.hideProgressLayout();
			}

			@Override
			protected void onPreExecute() {
				activity.showProgressLayout();
			}
		};
		backgroundTask.execute();

	}
}
