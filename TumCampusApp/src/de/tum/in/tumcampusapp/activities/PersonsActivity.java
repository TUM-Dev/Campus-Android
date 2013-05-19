package de.tum.in.tumcampusapp.activities;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.PersonListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.Employee;
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
public class PersonsActivity extends Activity implements OnEditorActionListener, TUMOnlineRequestFetchListener {

	/**
	 * Handler to send request to TUMOnline
	 */
	private TUMOnlineRequest requestHandler;

	/**
	 * Text field for the search tokens.
	 */
	private EditText etSearch;

	/**
	 * List to display the results
	 */
	private ListView lvPersons;

	private static final String PERSONEN_SUCHE = "personenSuche";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_persons);

		etSearch = (EditText) findViewById(R.id.etSearch);
		etSearch.setOnEditorActionListener(this);

		lvPersons = (ListView) findViewById(R.id.lstPersons);
	}
	
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
	
	private void searchForPersons() {
		requestHandler.setParameter("pSuche", etSearch.getText().toString());
		requestHandler.fetchInteractive(this, this);
	}

	@Override
	public void onStart() {
		super.onStart();

		requestHandler = new TUMOnlineRequest(PERSONEN_SUCHE, this);

	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

		if (etSearch.getText().length() < 2) {
			Utils.showLongCenteredToast(this, getString(R.string.please_insert_at_least_three_chars));
			return false;
		}

		Utils.hideKeyboard(this, etSearch);

		requestHandler.setParameter("pSuche", etSearch.getText().toString());

		// do the TUMOnline request (implement listener here)
		requestHandler.fetchInteractive(this, this);
		return true;
	}

	/**
	 * Handles the XML response from TUMOnline by deserializing the information to model entities.
	 * 
	 * @param rawResp The XML data from TUMOnline.
	 */
	@Override
	public void onFetch(String rawResp) {

		// test by sample element "familienname" (required field)
		if (!rawResp.contains("familienname")) {
			lvPersons.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
					new String[] { "keine Ergebnisse" }));
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
			return;
		}

		// fetch details about all employees separately
		EmploymentDetailsFetcher detailsFetchListener = new EmploymentDetailsFetcher(this, personList);
		detailsFetchListener.fetchEmploymentDetails();
	}

	@Override
	public void onFetchError(String errorReason) {
		Utils.showLongCenteredToast(PersonsActivity.this, errorReason);
		lvPersons.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
		// TODO Check Vasyl - to string.xml
				new String[] { getString(R.string.no_entries) }));
	}

	@Override
	public void onFetchCancelled() {
		// ignore
	}

	/**
	 * Displays the employees searched for.
	 * 
	 * @param employees The search results enriched with some additional information.
	 */
	private void displayResults(List<Person> employees) {
		final ListView lvStaff = (ListView) findViewById(R.id.lstPersons);

		lvStaff.setAdapter(new PersonListAdapter(PersonsActivity.this, employees));

		lvStaff.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				Object listViewItem = lvStaff.getItemAtPosition(position);
				Person person = (Person) listViewItem;

				// store selected person ID in bundle to get in in StaffDetails
				Bundle bundle = new Bundle();
				bundle.putSerializable("personObject", person);

				// show detailed information in new activity
				Intent intent = new Intent(PersonsActivity.this, PersonsDetailsActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	/**
	 * Helper class that successively fetches detail information for a list of employees.
	 * 
	 * @author Vincenz Doelle
	 * @review Daniel G. Mayr
	 */
	class EmploymentDetailsFetcher implements TUMOnlineRequestFetchListener {
		// Context activity (Staff.class)
		private final Activity context;

		// list of person IDs to be processed, used as a queue
		private final ArrayList<String> personIds;

		// number of persons to be processed
		private final int numberOfEmployees;

		// current number of persons processed
		private int numberEmployeesProcessed;

		private final ArrayList<Person> employees;

		// HTTP request handler to handle requests to TUMOnline
		@SuppressWarnings("hiding")
		TUMOnlineRequest requestHandler;

		public EmploymentDetailsFetcher(Activity context, PersonList personList) {
			// create new request handler
			requestHandler = new TUMOnlineRequest("personenDetails", context);
			this.context = context;
			this.employees = new ArrayList<Person>();

			personIds = new ArrayList<String>();
			for (Person person : personList.getPersons()) {
				personIds.add(person.getId());
			}

			numberOfEmployees = personList.getPersons().size();
			numberEmployeesProcessed = 0;
		}

		/**
		 * Fetch all persons whose IDs are given. Use personIds as a queue that is emptied successively.
		 */
		private void fetchEmploymentDetails() {

			// if all persons' details are fetched, display results and finish
			if (personIds.size() == 0) {
				displayResults(employees);
				return;
			}

			// get next person ID as parameter
			String parameterValue = personIds.listIterator().next();
			// remove this ID from the queue
			personIds.remove(parameterValue);

			// initialize request handler and update message for progress dialog
			// TODO Progress View
			// requestHandler.setProgressDialogMessage(numberEmployeesProcessed + "/" + numberOfEmployees
			// 		+ getString(R.string.personinformation_are_getting_fetched));
			requestHandler.setParameter("pIdentNr", parameterValue);
			requestHandler.fetchInteractive(context, this);
			numberEmployeesProcessed++;
		}

		@Override
		public void onFetch(String rawResp) {
			// deserialize XML response to model entities
			Serializer serializer = new Persister();
			try {
				Employee employee = serializer.read(Employee.class, rawResp);

				if (employee != null) {
					employees.add(employee);
					displayResults(employees);
				}

				// fetch next employment details
				fetchEmploymentDetails();

			} catch (Exception e) {
				e.printStackTrace();
				Log.d("EXCEPTION", e.getMessage());
			}
		}

		@Override
		public void onFetchError(String errorReason) {
			Utils.showLongCenteredToast(PersonsActivity.this, errorReason);
		}

		@Override
		public void onFetchCancelled() {
			// if user cancels the operation, display all results we have so far
			displayResults(employees);
		}

		@Override
		public void onCommonError(String errorReason) {
			// TODO Auto-generated method stub
			
		}

	}

	@Override
	public void onCommonError(String errorReason) {
		// TODO Auto-generated method stub
		
	}
}
