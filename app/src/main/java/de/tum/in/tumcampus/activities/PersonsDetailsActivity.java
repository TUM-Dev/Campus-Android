package de.tum.in.tumcampus.activities;

import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.auxiliary.HTMLStringBuffer;
import de.tum.in.tumcampus.models.Employee;
import de.tum.in.tumcampus.models.Group;
import de.tum.in.tumcampus.models.Person;
import de.tum.in.tumcampus.models.Room;
import de.tum.in.tumcampus.models.TelSubstation;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequestFetchListener;

/**
 * Activity to show information about an person at TUM.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
public class PersonsDetailsActivity extends ActivityForAccessingTumOnline
		implements TUMOnlineRequestFetchListener {

	private static final String PERSONEN_DETAILS = "personenDetails";

    public PersonsDetailsActivity() {
		super(PERSONEN_DETAILS, R.layout.activity_personsdetails);
	}

	/**
	 * Displays all relevant information about the given employee in the user
	 * interface (UI).
	 * 
	 * @param employee
	 *            The employee whose information should be displayed.
	 */
	private void displayResults(Employee employee) {
		// add the employee's counterfeit
		ImageView imageView = (ImageView) this.findViewById(R.id.ivImage);

		Bitmap image = employee.getImage();
		if (image == null) {
			image = BitmapFactory.decodeResource(getResources(),
					R.drawable.photo_not_available);
		}
		imageView.setImageBitmap(image);

		// use a custom string buffer that helps us with line feeds and
		// formatting
		HTMLStringBuffer contentText = new HTMLStringBuffer();

		TextView tvDetails1 = (TextView) findViewById(R.id.tvDetails1);

		// get the right gender
		if (employee.getGender() != null
				&& employee.getGender().equals(Person.MALE)) {
			contentText.append(getString(R.string.mr) + " ");
		} else if (employee.getGender() != null
				&& employee.getGender().equals(Person.FEMALE)) {
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
            for (Group group : groups) {
                if (group != null) {
                    contentText.appendField(getString(R.string.function),
                            group.getTitle());
                    contentText.appendField(getString(R.string.group), group.getOrg()
                            + " ("
                            + group.getId()
                            + ")" + "<br />");
                }
            }
		}
		tvDetails2.setText(Html.fromHtml(contentText.toString()),
				TextView.BufferType.SPANNABLE);

		// start new section

		contentText.clear();

		TextView tvDetails3 = (TextView) findViewById(R.id.tvDetails3);

		// add contact information, if available

		contentText.appendField(getString(R.string.email), employee.getEmail());
		contentText.appendField(getString(R.string.homepage), employee
				.getBusinessContact().getHomepage());

		List<TelSubstation> substations = employee.getTelSubstations();
		if (substations != null) {
			for (int i = 0; i < substations.size(); i++) {
				if (substations.get(i) != null) {
					contentText.appendField(getString(R.string.phone) + " "
							+ (i + 1), substations.get(i).getNumber());
				}

			}
		}
		contentText.appendField(getString(R.string.mobile_phone), employee
				.getBusinessContact().getMobilephone());
		contentText.appendField(getString(R.string.add_info), employee
				.getBusinessContact().getAdditionalInfo());
		tvDetails3.setText(Html.fromHtml(contentText.toString()),
				TextView.BufferType.SPANNABLE);

		// start new section
		contentText.clear();

		TextView tvDetails4 = (TextView) findViewById(R.id.tvDetails4);

		contentText.appendField(getString(R.string.office_hours),
				employee.getConsultationHours());

		// add all rooms
		List<Room> rooms = employee.getRooms();
		if (rooms != null && rooms.size() > 0) {
			contentText.appendField(getString(R.string.room), rooms.get(0)
					.getLocation() + " (" + rooms.get(0).getNumber() + ")");
		}

		tvDetails4.setText(Html.fromHtml(contentText.toString()),
				TextView.BufferType.SPANNABLE);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get person ID and/or object from Staff activity
		Bundle bundle = this.getIntent().getExtras();
		/*
	  The employee
	 */
        Person person = (Person) bundle.getSerializable("personObject");
		// make sure not both person is not null (error occurred)
		if (person == null) {
			// no query text specified
			Toast.makeText(this, getString(R.string.no_person_set),
					Toast.LENGTH_LONG).show();
			return;
		}
		// Sets the current name as a title
		setTitle(person.getName() + " " + person.getSurname());
		requestHandler.setParameter("pIdentNr", person.getId());
		super.requestFetch();
	}

	@Override
	public void onFetch(String rawResponse) {
		Log.d(getClass().getSimpleName(), rawResponse);

		// deserialize XML response to model entities
		Serializer serializer = new Persister();
		try {
			Employee employee = serializer.read(Employee.class, rawResponse);

			if (employee != null) {
				displayResults(employee);
				progressLayout.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("EXCEPTION", e.getMessage());
			progressLayout.setVisibility(View.GONE);
			errorLayout.setVisibility(View.VISIBLE);
		}
	}
}
