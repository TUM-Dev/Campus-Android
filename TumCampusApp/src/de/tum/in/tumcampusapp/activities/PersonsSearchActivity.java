package de.tum.in.tumcampusapp.activities;

import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.PersonListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.Person;
import de.tum.in.tumcampusapp.models.PersonList;

/**
 * Activity to search for employees.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 * @redesign Sascha Moecker
 */
public class PersonsSearchActivity extends ActivityForAccessingTumOnline implements OnEditorActionListener {
	private static final int MIN_SEARCH_LENGTH = 3;
	private static final String PERSONEN_SUCHE = "personenSuche";

	private Context context;

	/**
	 * Text field for the search tokens.
	 */
	private EditText etSearch;

	/**
	 * List to display the results
	 */
	private ListView lvPersons;

	public PersonsSearchActivity() {
		super(PERSONEN_SUCHE, R.layout.activity_persons);
		context = this;
	}

	/**
	 * Displays the employees searched for.
	 * 
	 * @param persons
	 *            The search results enriched with some additional information.
	 */
	private void displayResults(List<Person> persons) {
		final ListView lvStaff = (ListView) findViewById(R.id.lstPersons);

		lvStaff.setAdapter(new PersonListAdapter(this, persons));

		lvStaff.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				Object listViewItem = lvStaff.getItemAtPosition(position);
				Person person = (Person) listViewItem;

				// store selected person ID in bundle to get in in StaffDetails
				Bundle bundle = new Bundle();
				bundle.putSerializable("personObject", person);

				// show detailed information in new activity
				Intent intent = new Intent(context, PersonsDetailsActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	public void onClick(View view) {
		super.onClick(view);

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
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (etSearch.getText().length() < MIN_SEARCH_LENGTH) {
			Toast.makeText(this, R.string.please_insert_at_least_three_chars, Toast.LENGTH_SHORT).show();
			return false;
		}
		searchForPersons();
		return true;
	}

	/**
	 * Handles the XML response from TUMOnline by deserializing the information
	 * to model entities.
	 * 
	 * @param rawResponse
	 *            The XML data from TUMOnline.
	 */
	@Override
	public void onFetch(String rawResponse) {
		Log.d(getClass().getSimpleName(), rawResponse);

		// test by sample element "familienname" (required field)
		if (!rawResponse.contains("familienname")) {
			lvPersons.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] { "keine Ergebnisse" }));
		}

		Serializer serializer = new Persister();

		// Lists of employees
		PersonList personList = null;

		// deserialize the XML to model entities
		try {
			personList = serializer.read(PersonList.class, rawResponse);
		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			progressLayout.setVisibility(View.GONE);
			errorLayout.setVisibility(View.VISIBLE);
			Toast.makeText(this, R.string.no_search_result, Toast.LENGTH_SHORT).show();
			return;
		}
		displayResults(personList.getPersons());
		progressLayout.setVisibility(View.GONE);
	}

	private void searchForPersons() {
		Utils.hideKeyboard(this, etSearch);
		requestHandler.setParameter("pSuche", etSearch.getText().toString());
		super.requestFetch();
	}
}
