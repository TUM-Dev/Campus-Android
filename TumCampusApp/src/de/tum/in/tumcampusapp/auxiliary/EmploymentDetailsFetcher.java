package de.tum.in.tumcampusapp.auxiliary;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.PersonsActivity;
import de.tum.in.tumcampusapp.activities.PersonsDetailsActivity;
import de.tum.in.tumcampusapp.adapters.PersonListAdapter;
import de.tum.in.tumcampusapp.models.Employee;
import de.tum.in.tumcampusapp.models.Person;
import de.tum.in.tumcampusapp.models.PersonList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

/**
 * Helper class that successively fetches detail information for a list of
 * employees.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 */
public class EmploymentDetailsFetcher extends Activity implements TUMOnlineRequestFetchListener {
	// Context activity (Staff.class)
	private final Activity context;

	private final ArrayList<Person> employees;

	// current number of persons processed
	private int numberEmployeesProcessed;

	// number of persons to be processed
	private final int numberOfEmployees;

	// list of person IDs to be processed, used as a queue
	private final ArrayList<String> personIds;

	// HTTP request handler to handle requests to TUMOnline
	@SuppressWarnings("hiding")
	TUMOnlineRequest requestHandler;
	
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
				Intent intent = new Intent(getApplicationContext(), PersonsDetailsActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

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
	 * Fetch all persons whose IDs are given. Use personIds as a queue that is
	 * emptied successively.
	 */
	public void fetchEmploymentDetails() {

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
		// requestHandler.setProgressDialogMessage(numberEmployeesProcessed
		// + "/" + numberOfEmployees
		// + getString(R.string.personinformation_are_getting_fetched));
		requestHandler.setParameter("pIdentNr", parameterValue);
		requestHandler.fetchInteractive(context, this);
		numberEmployeesProcessed++;
	}

	@Override
	public void onCommonError(String errorReason) {
		// TODO Auto-generated method stub

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
	public void onFetchCancelled() {
		// if user cancels the operation, display all results we have so far
		displayResults(employees);
	}

	@Override
	public void onFetchError(String errorReason) {
		Utils.showLongCenteredToast(this, errorReason);
	}

}
