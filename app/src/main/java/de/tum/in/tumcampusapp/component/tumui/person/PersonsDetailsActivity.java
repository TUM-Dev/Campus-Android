package de.tum.in.tumcampusapp.component.tumui.person;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.person.model.Contact;
import de.tum.in.tumcampusapp.component.tumui.person.model.Employee;
import de.tum.in.tumcampusapp.component.tumui.person.model.Group;
import de.tum.in.tumcampusapp.component.tumui.person.model.Person;
import de.tum.in.tumcampusapp.component.tumui.person.model.Room;
import de.tum.in.tumcampusapp.component.tumui.person.model.TelSubstation;
import de.tum.in.tumcampusapp.utils.HTMLStringBuffer;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Activity to show information about an person at TUM.
 */
public class PersonsDetailsActivity extends ActivityForAccessingTumOnline<Employee> {

    private static final String[] PERMISSIONS_CONTACTS = {Manifest.permission.WRITE_CONTACTS};
    private Employee mEmployee;
    private MenuItem mContact;

    public PersonsDetailsActivity() {
        super(TUMOnlineConst.Companion.getPERSON_DETAILS(), R.layout.activity_personsdetails);
    }

    private static void addContact(Collection<ContentProviderOperation> ops, int rawContactID, Contact contact, boolean work) {
        if (contact != null) {
            // Add work telefon number
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                            .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                                            .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                                            .withValue(Phone.NUMBER, contact.getTelefon())
                                            .withValue(Phone.TYPE, work ? Phone.TYPE_WORK : Phone.TYPE_HOME)
                                            .build());

            // Add work mobile number
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                            .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                                            .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                                            .withValue(Phone.NUMBER, contact.getMobilephone())
                                            .withValue(Phone.TYPE, work ? Phone.TYPE_WORK_MOBILE : Phone.TYPE_MOBILE)
                                            .build());
            // Add work fax number
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                            .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                                            .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                                            .withValue(Phone.NUMBER, contact.getFax())
                                            .withValue(Phone.TYPE, work ? Phone.TYPE_FAX_WORK : Phone.TYPE_FAX_HOME)
                                            .build());
            // Add website
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                            .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                                            .withValue(Data.MIMETYPE, Website.CONTENT_ITEM_TYPE)
                                            .withValue(Website.URL, contact.getHomepage())
                                            .withValue(Website.TYPE, work ? Website.TYPE_WORK : Website.TYPE_HOME)
                                            .build());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get person from staff activity
        Bundle bundle = this.getIntent()
                            .getExtras();
        Person person = (Person) bundle.getSerializable("personObject");

        // make sure not both person is not null (error occurred)
        if (person == null) {
            // no query text specified
            Utils.showToast(this, R.string.no_person_set);
            return;
        }

        // Sets the current name as a title
        setTitle(person.getName() + ' ' + person.getSurname());
        requestHandler.setParameter("pIdentNr", person.getId());
        super.requestFetch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_add_contact, menu);
        mContact = menu.findItem(R.id.action_add_contact);
        if (mEmployee == null) {
            mContact.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_add_contact) {
            if (mEmployee != null) {
                AlertDialog.Builder dialog= new AlertDialog.Builder(this);
                dialog.setTitle(R.string.dialog_add_to_contacts);
                dialog.setPositiveButton(R.string.yes, (dialog1, which) -> addContact(mEmployee));
                dialog.setNegativeButton(R.string.no, null);
                dialog.setIcon(R.drawable.ic_action_add_person_blue);
                dialog.show();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFetch(Employee rawResponse) {
        mEmployee = rawResponse;
        displayResults(mEmployee);
        if (mContact != null) {
            mContact.setVisible(true);
        }
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
        ImageView imageView = this.findViewById(R.id.ivImage);

        Bitmap image = employee.getImage();
        if (image == null) {
            image = BitmapFactory.decodeResource(getResources(),
                                                 R.drawable.photo_not_available_rectangular);
        }
        imageView.setImageBitmap(image);

        // use a custom string buffer that helps us with line feeds and
        // formatting
        HTMLStringBuffer contentText = new HTMLStringBuffer();

        TextView tvDetails1 = findViewById(R.id.tvDetails1);

        // get the right gender
        if (employee.getGender().equals(Person.Companion.getMALE())) {
            contentText.append(getString(R.string.mr) + ' ');
        } else if (employee.getGender().equals(Person.Companion.getFEMALE())) {
            contentText.append(getString(R.string.mrs) + ' ');
        }

        // add title if available
        contentText.append(employee.getTitle() + ' ');

        // add name
        contentText.append(employee.getName() + ' ' + employee.getSurname());
        tvDetails1.setText(contentText.toString());

        // start new information section

        contentText.clear();

        TextView tvDetails2 = findViewById(R.id.tvDetails2);

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
                                                                       + ")<br />");
                }
            }
        }
        tvDetails2.setText(Utils.fromHtml(contentText.toString()),
                           TextView.BufferType.SPANNABLE);

        // start new section

        contentText.clear();

        TextView tvDetails3 = findViewById(R.id.tvDetails3);

        // add contact information, if available

        contentText.appendField(getString(R.string.email), employee.getEmail());
        contentText.appendField(getString(R.string.homepage), employee
                .getBusinessContact()
                .getHomepage());

        List<TelSubstation> substations = employee.getTelSubstations();
        if (substations != null) {
            for (int i = 0; i < substations.size(); i++) {
                if (substations.get(i) != null) {
                    contentText.appendField(getString(R.string.phone) + ' '
                                            + i + 1, substations.get(i)
                                                                .getNumber());
                }

            }
        }
        contentText.appendField(getString(R.string.mobile_phone), employee
                .getBusinessContact()
                .getMobilephone());
        contentText.appendField(getString(R.string.add_info), employee
                .getBusinessContact()
                .getAdditionalInfo());
        tvDetails3.setText(Utils.fromHtml(contentText.toString()),
                           TextView.BufferType.SPANNABLE);

        // start new section
        contentText.clear();

        TextView tvDetails4 = findViewById(R.id.tvDetails4);

        contentText.appendField(getString(R.string.office_hours),
                                employee.getConsultationHours());

        // add all rooms
        List<Room> rooms = employee.getRooms();
        if (rooms != null && !rooms.isEmpty()) {
            contentText.appendField(getString(R.string.room), rooms.get(0)
                                                                   .getLocation() + " (" + rooms.get(0)
                                                                                                .getNumber() + ')');
        }

        tvDetails4.setText(Utils.fromHtml(contentText.toString()),
                           TextView.BufferType.SPANNABLE);

    }

    /**
     * Adds the given employee to the users contact list
     *
     * @param employee Object to insert into contacts
     */
    private void addContact(Employee employee) {
        if (!isPermissionGranted(0)) {
            return;
        }
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

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
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                                        .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                                        .withValue(Email.DATA, employee.getEmail())
                                        .withValue(Email.TYPE, Email.TYPE_WORK)
                                        .build());

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
                                                .withValue(CommonDataKinds.Organization.TYPE, Organization.TYPE_WORK)
                                                .build());
            }
        }

        // Add office hours
        StringBuilder notes = new StringBuilder();
        notes.append(getString(R.string.office_hours))
             .append(": ")
             .append(employee.getConsultationHours());

        // add all rooms
        List<Room> rooms = employee.getRooms();
        if (rooms != null && !rooms.isEmpty()) {
            if (!notes.toString()
                      .isEmpty()) {
                notes.append('\n');
            }
            notes.append(getString(R.string.room))
                 .append(": ")
                 .append(rooms.get(0)
                              .getLocation())
                 .append(" (")
                 .append(rooms.get(0)
                              .getNumber())
                 .append(')');
        }

        // Finally add notes
        if (!notes.toString()
                  .isEmpty()) {
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                            .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                                            .withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
                                            .withValue(Note.NOTE, notes.toString())
                                            .build());
        }

        // Add image
        Bitmap bitmap = employee.getImage();
        if (bitmap != null) {    // If an image is selected successfully
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
            } catch (IOException e) {
                Utils.log(e);
            }
        }

        // Executing all the insert operations as a single database transaction
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Utils.showToast(this, R.string.contact_added);
        } catch (RemoteException | OperationApplicationException e) {
            Utils.log(e);
        }
    }

    /**
     * Check Contacts permission for Android 6.0
     *
     * @param id the request id
     * @return If the contacts permission was granted
     */
    private boolean isPermissionGranted(final int id) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {

                // Display an AlertDialog with an explanation and a button to trigger the request.
                new AlertDialog.Builder(this)
                        .setMessage(R.string.permission_contacts_explanation)
                        .setPositiveButton(R.string.grant_permission, (dialog, onClickId) -> ActivityCompat
                                .requestPermissions(PersonsDetailsActivity.this, PERMISSIONS_CONTACTS, id))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACTS, id);
            }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Check if we got all Calendar permissions
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        //Rerun the interrupted action
        addContact(mEmployee);
    }
}
