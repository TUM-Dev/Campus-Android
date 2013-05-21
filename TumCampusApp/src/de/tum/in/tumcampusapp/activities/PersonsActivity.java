package de.tum.in.tumcampusapp.activities;

import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.PersonListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.Person;
import de.tum.in.tumcampusapp.models.PersonList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

/**
 * Activity to search for employees.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
public class PersonsActivity extends ActivityForAccessingTumOnline implements OnEditorActionListener, TUMOnlineRequestFetchListener {

	public PersonsActivity() {
		super(PERSONEN_SUCHE, R.layout.activity_persons);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	private static final String PERSONEN_SUCHE = "personenSuche";

	/**
	 * Text field for the search tokens.
	 */
	private EditText etSearch;

	/**
	 * List to display the results
	 */
	private ListView lvPersons;


	public void onClick(View view) {
		int viewId = view.getId();
		switch (viewId) {
		case R.id.clear:
			etSearch.setText("");
			break;
		case R.id.dosearch:
			searchForPersons();
			break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		etSearch = (EditText) findViewById(R.id.etSearch);
		etSearch.setOnEditorActionListener(this);

		lvPersons = (ListView) findViewById(R.id.lstPersons);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (etSearch.getText().length() < 2) {
			Utils.showLongCenteredToast(this, getString(R.string.please_insert_at_least_three_chars));
			return false;
		}
		searchForPersons();
		return true;
	}

	/**
	 * Handles the XML response from TUMOnline by deserializing the information
	 * to model entities.
	 * 
	 * @param rawResp
	 *            The XML data from TUMOnline.
	 */
	@Override
	public void onFetch(String rawResp) {

		// test by sample element "familienname" (required field)
		if (!rawResp.contains("familienname")) {
			lvPersons.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] { "keine Ergebnisse" }));
		}

		Serializer serializer = new Persister();

		// Lists of employees
		PersonList personList = null;

		// deserialize the XML to model entities
		try {
			personList = serializer.read(PersonList.class, rawResp);
		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			e.printStackTrace();
			progressLayout.setVisibility(View.GONE);
			errorLayout.setVisibility(View.VISIBLE);
			return;
		}
		displayResults(personList.getPersons());
		progressLayout.setVisibility(View.GONE);
	}

	/**
	 * Displays the employees searched for.
	 * 
	 * @param employees
	 *            The search results enriched with some additional information.
	 */
	private void displayResults(List<Person> employees) {
		final ListView lvStaff = (ListView) findViewById(R.id.lstPersons);

		lvStaff.setAdapter(new PersonListAdapter(this, employees));

		lvStaff.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				Object listViewItem = lvStaff.getItemAtPosition(position);
				Person person = (Person) listViewItem;

				// store selected person ID in bundle to get in in StaffDetails
				Bundle bundle = new Bundle();
				bundle.putSerializable("personObject", person);

				// show detailed information in new activity
				Intent intent = new Intent(getParent(), PersonsDetailsActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	private void searchForPersons() {
		Utils.hideKeyboard(this, etSearch);
		requestHandler.setParameter("pSuche", etSearch.getText().toString());
		super.requestFetch();
	}
}
