package de.tum.`in`.tumcampusapp.component.tumui.person

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems.*
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Employee
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.ContactsHelper
import kotlinx.android.synthetic.main.activity_person_details.*

/**
 * Activity to show information about a person at TUM.
 */
class PersonDetailsActivity : ActivityForAccessingTumOnline<Employee>(R.layout.activity_person_details) {

    private lateinit var personId: String
    private var employee: Employee? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val person = intent.extras?.getSerializable("personObject") as? Person
        if (person == null) {
            finish()
            return
        }

        personId = person.id
        title = person.getFullName()

        loadPersonDetails(person.id, CacheControl.USE_CACHE)
    }

    override fun onRefresh() {
        loadPersonDetails(personId, CacheControl.BYPASS_CACHE)
    }

    private fun loadPersonDetails(personId: String, cacheControl: CacheControl) {
        val apiCall = apiClient.getPersonDetails(personId, cacheControl)
        fetch(apiCall)
    }

    override fun onDownloadSuccessful(response: Employee) {
        this.employee = response
        displayResult(response)
        invalidateOptionsMenu()
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
                .setMessage(R.string.dialog_add_to_contacts)
                .setPositiveButton(R.string.add) { _, _ -> addContact(employee) }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    /**
     * Displays all relevant information about the given employee in the user
     * interface (UI).
     *
     * @param employee The employee whose information should be displayed.
     */
    private fun displayResult(employee: Employee) {
        scrollView.visibility = View.VISIBLE

        val image = employee.image ?: BitmapFactory.decodeResource(
                resources, R.drawable.photo_not_available)

        pictureImageView.setImageBitmap(image)
        nameTextView.text = employee.getNameWithTitle(this)

        // Set up employee groups
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
            if (employee.email.isNotBlank()) {
                add(EmailContactItem(employee.email))
            }

            employee.businessContact?.homepage?.let { homepage ->
                if (homepage.isNotBlank()) {
                    add(HomepageContactItem(homepage))
                }
            }
        }

        employee.telSubstations?.forEach { station ->
            if (station.number.isNotBlank()) {
                contactItems.add(PhoneContactItem(station.number))
            }
        }

        employee.businessContact?.mobilephone?.let { mobilephone ->
            if (mobilephone.isNotBlank()) {
                contactItems.add(MobilePhoneContactItem(mobilephone))
            }
        }

        employee.businessContact?.additionalInfo?.let { additionalInfo ->
            if (additionalInfo.isNotBlank()) {
                contactItems.add(InformationContactItem(additionalInfo))
            }
        }

        if (employee.consultationHours.isNotBlank()) {
            contactItems.add(OfficeHoursContactItem(employee.consultationHours))
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
        if (!isPermissionGranted(Const.CONTACTS_PERMISSION_REQUEST_CODE)) {
            return
        }

        if (employee != null) {
            ContactsHelper.saveToContacts(this, employee)
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
        }

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
