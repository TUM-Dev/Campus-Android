package de.tum.in.tumcampus.activities;

import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.auxiliary.HTMLStringBuffer;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.Contact;
import de.tum.in.tumcampus.models.Employee;
import de.tum.in.tumcampus.models.Group;
import de.tum.in.tumcampus.models.Person;
import de.tum.in.tumcampus.models.Room;
import de.tum.in.tumcampus.models.TelSubstation;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;

/**
 * Activity to show information about an person at TUM.
 */
public class PersonsDetailsActivity extends ActivityForAccessingTumOnline<Employee> {

    private Employee mEmployee;
    private MenuItem mContact;

    public PersonsDetailsActivity() {
		super(TUMOnlineConst.PERSON_DETAILS, R.layout.activity_personsdetails);
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get person from staff activity
        Bundle bundle = this.getIntent().getExtras();
        Person person = (Person) bundle.getSerializable("personObject");

        // make sure not both person is not null (error occurred)
        if (person == null) {
            // no query text specified
            Utils.showToast(this, R.string.no_person_set);
            return;
        }

        // Sets the current name as a title
        setTitle(person.getName() + " " + person.getSurname());
        requestHandler.setParameter("pIdentNr", person.getId());
        super.requestFetch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_add_contact, menu);
        mContact = menu.findItem(R.id.action_add_contact);
        if(mEmployee==null) {
            mContact.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_contact:
                if(mEmployee!=null)
                    addContact(mEmployee);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFetch(Employee rawResponse) {
        mEmployee = rawResponse;
        displayResults(mEmployee);
        mContact.setVisible(true);
        showLoadingEnded();
    }

    /**
	 * Displays all relevant information about the given employee in the user
	 * interface (UI).
	 * 
	 * @param employee The employee whose information should be displayed.
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

    /**
     * Adds the given employee to the users contact list
     * @param employee Object to insert into contacts
     */
    private void addContact(Employee employee) {
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();

        int rawContactID = ops.size();

        // Adding insert operation to operations list
        // to insert a new raw contact in the table ContactsContract.RawContacts
        ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .build());

        // Add full name
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.PREFIX, employee.getTitle())
                .withValue(StructuredName.GIVEN_NAME, employee.getName())
                .withValue(StructuredName.FAMILY_NAME, employee.getSurname())
                .build());

        // Add e-mail address
        if (employee.getEmail() != null) {
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Email.DATA, employee.getEmail())
                    .withValue(CommonDataKinds.Email.TYPE, Email.TYPE_WORK)
                    .build());
        }

        List<TelSubstation> substations = employee.getTelSubstations();
        if (substations != null) {
            for (TelSubstation sub : substations) {
                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, sub.getNumber())
                        .withValue(Phone.TYPE, Phone.TYPE_WORK)
                        .build());
            }
        }

        // Add work: telefon, mobile, fax, website
        addContact(ops, rawContactID, employee.getBusinessContact(), true);

        // Add home: telefon, mobile, fax, website
        addContact(ops, rawContactID, employee.getPrivateContact(), false);

        // Add organisations
        if (employee.getGroups() != null) {
            for (Group group : employee.getGroups()) {
                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(Data.MIMETYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.Organization.COMPANY, group.getOrg())
                        .withValue(Data.MIMETYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.Organization.TITLE, group.getTitle())
                        .withValue(Data.MIMETYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.Organization.TYPE, Organization.TYPE_WORK).build());
            }
        }

        // Add office hours
        String notes = "";
        if (employee.getConsultationHours() != null) {
            notes = getString(R.string.office_hours)+": "+employee.getConsultationHours();
        }

        // add all rooms
        List<Room> rooms = employee.getRooms();
        if (rooms != null && rooms.size() > 0) {
            if(!notes.isEmpty())
                notes+="\n";
            notes += getString(R.string.room)+": "+rooms.get(0).getLocation() + " (" + rooms.get(0).getNumber() + ")";
        }

        // Finally add notes
        if(!notes.isEmpty()) {
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
                    .withValue(Note.NOTE, notes)
                    .build());
        }

        // Add image
        Bitmap bitmap = employee.getImage();
        if(bitmap!=null){    // If an image is selected successfully
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 75, stream);

            // Adding insert operation to operations list
            // to insert Photo in the table ContactsContract.Data
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(Data.IS_SUPER_PRIMARY, 1)
                    .withValue(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Photo.PHOTO, stream.toByteArray())
                    .build());

            try {
                stream.flush();
            }catch (IOException e) {
                Utils.log(e);
            }
        }

        // Executing all the insert operations as a single database transaction
        try{
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Utils.showToast(this, R.string.contact_added);
        }catch (RemoteException | OperationApplicationException e) {
            Utils.log(e);
        }
    }

    private static void addContact(ArrayList<ContentProviderOperation> ops, int rawContactID, Contact contact, boolean work) {
        if(contact!=null) {
            // Add work telefon number
            if(contact.getTelefon()!=null) {
                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, contact.getTelefon())
                        .withValue(Phone.TYPE, work?Phone.TYPE_WORK:Phone.TYPE_HOME)
                        .build());
            }
            // Add work mobile number
            if(contact.getMobilephone()!=null) {
                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, contact.getMobilephone())
                        .withValue(Phone.TYPE, work?Phone.TYPE_WORK_MOBILE:Phone.TYPE_MOBILE)
                        .build());
            }
            // Add work fax number
            if(contact.getFax()!=null) {
                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, contact.getFax())
                        .withValue(Phone.TYPE, work?Phone.TYPE_FAX_WORK:Phone.TYPE_FAX_HOME)
                        .build());
            }
            // Add website
            if(contact.getHomepage()!=null) {
                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(Data.MIMETYPE, Website.CONTENT_ITEM_TYPE)
                        .withValue(Website.URL, contact.getHomepage())
                        .withValue(Website.TYPE, work?Website.TYPE_WORK:Website.TYPE_HOME)
                        .build());
            }
        }
    }
}
