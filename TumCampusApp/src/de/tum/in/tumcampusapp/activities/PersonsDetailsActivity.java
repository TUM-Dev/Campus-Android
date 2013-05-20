package de.tum.in.tumcampusapp.activities;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.HTMLStringBuffer;
import de.tum.in.tumcampusapp.models.Employee;
import de.tum.in.tumcampusapp.models.Group;
import de.tum.in.tumcampusapp.models.Person;
import de.tum.in.tumcampusapp.models.Room;
import de.tum.in.tumcampusapp.models.TelSubstation;

/**
 * Activity to show information about an employee.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
public class PersonsDetailsActivity extends Activity {

	/**
	 * The employee
	 */
	private Person personObject;

	/**
	 * Displays all relevant information about the given employee in the user interface (UI).
	 * 
	 * @param employee The employee whose information should be displayed.
	 */
	private void initUI(Employee employee) {

		// add the employee's counterfeit
		ImageView image = (ImageView) this.findViewById(R.id.ivImage);
		image.setImageBitmap(employee.getImage());

		// use a custom string buffer that helps us with line feeds and formatting
		HTMLStringBuffer contentText = new HTMLStringBuffer();

		TextView tvDetails1 = (TextView) findViewById(R.id.tvDetails1);

		// get the right gender
		if (employee.getGender() != null && employee.getGender().equals(Person.MALE)) {
			contentText.append(getString(R.string.mr) + " ");
		} else if (employee.getGender() != null && employee.getGender().equals(Person.FEMALE)) {
			contentText.append(getString(R.string.mrs) + " ");
		}

		// add title if available
		if (employee.getTitle() != null) {
			contentText.append(employee.getTitle() + " ");
		}

		// add name
		contentText.append(employee.getName() + " " + employee.getSurname());
		tvDetails1.setText(contentText.toString());

		// start new information section

		contentText.clear();

		TextView tvDetails2 = (TextView) findViewById(R.id.tvDetails2);

		// add all groups the employee belongs to
		List<Group> groups = employee.getGroups();
		if (groups != null) {
			for (int i = 0; i < groups.size(); i++) {
				if (groups.get(i) != null) {
					contentText.appendField(getString(R.string.function), groups.get(i).getTitle());
					contentText.appendField(getString(R.string.group), groups.get(i).getOrg() + " ("
							+ groups.get(i).getId() + ")" + "<br />");
				}
			}
		}
		tvDetails2.setText(Html.fromHtml(contentText.toString()), TextView.BufferType.SPANNABLE);

		// start new section

		contentText.clear();

		TextView tvDetails3 = (TextView) findViewById(R.id.tvDetails3);

		// add contact information, if available

		contentText.appendField(getString(R.string.email), employee.getEmail());
		contentText.appendField(getString(R.string.homepage), employee.getBusinessContact().getHomepage());

		List<TelSubstation> substations = employee.getTelSubstations();
		if (substations != null) {
			for (int i = 0; i < substations.size(); i++) {
				if (substations.get(i) != null) {
					contentText.appendField(getString(R.string.phone) + " " + (i + 1), substations.get(i).getNumber());
				}

			}
		}
		contentText.appendField(getString(R.string.mobile_phone), employee.getBusinessContact().getMobilephone());
		contentText.appendField(getString(R.string.add_info), employee.getBusinessContact().getAdditionalInfo());
		tvDetails3.setText(Html.fromHtml(contentText.toString()), TextView.BufferType.SPANNABLE);

		// start new section
		contentText.clear();

		TextView tvDetails4 = (TextView) findViewById(R.id.tvDetails4);

		contentText.appendField(getString(R.string.office_hours), employee.getConsultationHours());

		// add all rooms
		List<Room> rooms = employee.getRooms();
		if (rooms != null && rooms.size() > 0) {
			contentText.appendField(getString(R.string.room), rooms.get(0).getLocation() + " ("
					+ rooms.get(0).getNumber() + ")");
		}

		tvDetails4.setText(Html.fromHtml(contentText.toString()), TextView.BufferType.SPANNABLE);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_personsdetails);

		// get person ID and/or object from Staff activity
		Bundle bundle = this.getIntent().getExtras();
		personObject = (Person) bundle.getSerializable("personObject");
	}

	@Override
	public void onStart() {
		super.onStart();

		// make sure not both person is not null (error occurred)
		if (personObject == null) {
			// no query text specified
			Toast.makeText(this, getString(R.string.no_person_set), Toast.LENGTH_LONG).show();
			return;
		}

		initUI((Employee) personObject);
	}
}
