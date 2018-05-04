package de.tum.`in`.tumcampusapp.component.tumui.person

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineConst
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems.*
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Employee
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person
import de.tum.`in`.tumcampusapp.utils.ContactsManager
import kotlinx.android.synthetic.main.activity_person_details.*

/**
 * Activity to show information about an person at TUM.
 */
class PersonDetailsActivity : ActivityForAccessingTumOnline<Employee>(TUMOnlineConst.PERSON_DETAILS, R.layout.activity_person_details) {

    private var employee: Employee? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent.extras
        if (bundle == null) {
            finish()
            return
        }

        val person = bundle.getSerializable("personObject") as Person

        title = person.getFullName()
        requestHandler.setParameter("pIdentNr", person.id)
        super.requestFetch()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_add_contact, menu)
        val addToContactsItem = menu.findItem(R.id.action_add_contact)
        addToContactsItem.isVisible = (employee != null)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_contact -> {
                displayAddContactDialog(employee)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayAddContactDialog(employee: Employee?) {
        if (employee == null) {
            return
        }

        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_add_to_contacts)
                .setPositiveButton(R.string.add, { _, _ -> addContact(employee) })
                .setNegativeButton(R.string.cancel, { dialog, _ -> dialog.dismiss() })
                .setIcon(R.drawable.ic_action_add_person_blue)
                .show()
    }

    override fun onFetch(response: Employee) {
        employee = response
        displayResult(response)
        invalidateOptionsMenu()
        showLoadingEnded()
    }

    /**
     * Displays all relevant information about the given employee in the user
     * interface (UI).
     *
     * @param employee The employee whose information should be displayed.
     */
    private fun displayResult(employee: Employee) {
        val image = employee.image ?: BitmapFactory.decodeResource(
                resources, R.drawable.photo_not_available)

        pictureImageView.setImageBitmap(image)
        nameTextView.text = employee.getNameWithTitle(this)

        // Setup employee groups
        val groups = employee.groups
        if (groups?.isNotEmpty() == true) {
            groupsRecyclerView.setHasFixedSize(true)
            groupsRecyclerView.layoutManager = LinearLayoutManager(this)
            groupsRecyclerView.adapter = PersonGroupsAdapter(groups)
        } else {
            dividerNameGroups.visibility = View.GONE
            groupsRecyclerView.visibility = View.GONE
        }

        // Setup contact items
        val contactItems = arrayListOf<AbstractContactItem>().apply {
            add(EmailContactItem(employee.email))
            employee.businessContact?.let { contact ->
                if (contact.homepage.isNotBlank()) {
                    add(WebsiteContactItem(contact.homepage))
                }
            }
        }

        employee.telSubstations?.forEach { station ->
            if (station.number.isNotBlank()) {
                contactItems.add(PhoneContactItem(station.number))
            }
        }

        employee.businessContact?.let { contact ->
            if (contact.mobilephone.isNotBlank()) {
                contactItems.add(MobilePhoneContactItem(contact.mobilephone))
            }

            if (contact.additionalInfo.isNotBlank()) {
                contactItems.add(InformationContactItem(contact.additionalInfo))
            }
        }

        if (employee.consultationHours.isNotBlank()) {
            contactItems.add(ConsultationHoursContactItem(employee.consultationHours))
        }

        employee.rooms?.let { rooms ->
            rooms.forEach { room ->
                contactItems.add(RoomContactItem(room.getFullLocation(), room.number))
            }
        }

        if (contactItems.isNotEmpty()) {
            contactItemsRecyclerView.setHasFixedSize(true)
            contactItemsRecyclerView.layoutManager = LinearLayoutManager(this)
            contactItemsRecyclerView.adapter = PersonContactItemsAdapter(contactItems)
        } else {
            dividerGroupsContactItems.visibility = View.GONE
            contactItemsRecyclerView.visibility = View.GONE
        }
    }

    /**
     * Adds the given employee to the users contact list
     *
     * @param employee Object to insert into contacts
     */
    private fun addContact(employee: Employee?) {
        if (!isPermissionGranted(0)) {
            return
        }

        if (employee != null) {
            ContactsManager.saveToContacts(this, employee)
        }
    }

    /**
     * Check Contacts permission for Android 6.0
     *
     * @param id the request id
     * @return If the contacts permission was granted
     */
    private fun isPermissionGranted(id: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        } else {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {
                // Display an AlertDialog with an explanation and a button to trigger the request.
                AlertDialog.Builder(this)
                        .setMessage(R.string.permission_contacts_explanation)
                        .setPositiveButton(R.string.grant_permission) { _, _ ->
                            ActivityCompat.requestPermissions(this@PersonDetailsActivity, PERMISSIONS_CONTACTS, id)
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACTS, id)
            }
        }

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            // Rerun the interrupted action
            addContact(employee)
        }
    }

    companion object {
        private val PERMISSIONS_CONTACTS = arrayOf(Manifest.permission.WRITE_CONTACTS)
    }

}
