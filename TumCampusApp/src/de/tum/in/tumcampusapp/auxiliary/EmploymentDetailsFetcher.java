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
public class EmploymentDetailsFetcher implements TUMOnlineRequestFetchListener {
	// Context activity (Staff.class)
	private final Activity context;
	private Person person;

	// HTTP request handler to handle requests to TUMOnline
	@SuppressWarnings("hiding")
	TUMOnlineRequest requestHandler;

	/**
	 * Displays the employees searched for.
	 * 
	 * @param employees
	 *            The search results enriched with some additional information.
	 */
	private void displayResults(Person employees) {
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

	public EmploymentDetailsFetcher(Activity context, Person person) {
		// create new request handler
		requestHandler = new TUMOnlineRequest("personenDetails", context);
		this.context = context;
		this.person = person;
	}

	/**
	 * Fetch all persons whose IDs are given. Use personIds as a queue that is
	 * emptied successively.
	 */
	public void fetchEmploymentDetails() {
		// initialize request handler and update message for progress dialog
		// TODO Progress View
		// requestHandler.setProgressDialogMessage(numberEmployeesProcessed
		// + "/" + numberOfEmployees
		// + getString(R.string.personinformation_are_getting_fetched));
		requestHandler.setParameter("pIdentNr", person.getId());
		requestHandler.fetchInteractive(context, this);

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
				displayResults(employee);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("EXCEPTION", e.getMessage());
		}
	}

	@Override
	public void onFetchCancelled() {
		// TODO
	}

	@Override
	public void onFetchError(String errorReason) {
		Utils.showLongCenteredToast(this.context, errorReason);
	}
}
