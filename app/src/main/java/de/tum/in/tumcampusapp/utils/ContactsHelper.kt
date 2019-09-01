package de.tum.`in`.tumcampusapp.utils

import android.content.ContentProviderOperation
import android.content.Context
import android.content.OperationApplicationException
import android.graphics.Bitmap
import android.os.RemoteException
import android.provider.ContactsContract
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Contact
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Employee
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class ContactsHelper {

    companion object {

        @JvmStatic fun saveToContacts(context: Context, employee: Employee) {
            val ops = ArrayList<ContentProviderOperation>()

            val rawContactID = ops.size

            // Adding insert operation to operations list
            // to insert a new raw contact in the table ContactsContract.RawContacts
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build())

            // Add full name
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.PREFIX, employee.title)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, employee.name)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, employee.surname)
                    .build())

            // Add e-mail address
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, employee.email)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build())

            val substations = employee.telSubstations
            if (substations != null) {
                for ((number) in substations) {
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                            .build())
                }
            }

            // Add work: telephone, mobile, fax, website
            addContact(ops, rawContactID, employee.businessContact, true)

            // Add home: telephone, mobile, fax, website
            addContact(ops, rawContactID, employee.privateContact, false)

            // Add organisations
            employee.groups?.let { groups ->
                groups.forEach { group ->
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, group.org)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, group.title)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                            .build())
                }
            }

            // Add office hours
            val notes = StringBuilder()
            notes.append(context.getString(R.string.office_hours))
                    .append(": ")
                    .append(employee.consultationHours)

            // saveToContacts all rooms
            val rooms = employee.rooms
            if (rooms != null && !rooms.isEmpty()) {
                if (!notes.toString()
                                .isEmpty()) {
                    notes.append('\n')
                }
                notes.append(context.getString(R.string.room))
                        .append(": ")
                        .append(rooms[0]
                                .location)
                        .append(" (")
                        .append(rooms[0]
                                .number)
                        .append(')')
            }

            // Finally saveToContacts notes
            if (!notes.toString()
                            .isEmpty()) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, notes.toString())
                        .build())
            }

            // Add image
            val bitmap = employee.image
            if (bitmap != null) { // If an image is selected successfully
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 75, stream)

                // Adding insert operation to operations list
                // to insert Photo in the table ContactsContract.Data
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray())
                        .build())

                try {
                    stream.flush()
                } catch (e: IOException) {
                    Utils.log(e)
                }
            }

            // Executing all the insert operations as a single database transaction
            try {
                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
                Utils.showToast(context, R.string.contact_added)
            } catch (e: RemoteException) {
                Utils.log(e)
            } catch (e: OperationApplicationException) {
                Utils.log(e)
            }
        }

        private fun addContact(ops: MutableCollection<ContentProviderOperation>, rawContactID: Int, contact: Contact?, work: Boolean) {
            if (contact != null) {
                // Add work telephone number
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.telefon)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                if (work) ContactsContract.CommonDataKinds.Phone.TYPE_WORK else ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                        .build())

                // Add work mobile number
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.mobilephone)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                if (work) ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE else ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build())
                // Add work fax number
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.fax)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                if (work) ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK else ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME)
                        .build())
                // Add website
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Website.URL, contact.homepage)
                        .withValue(ContactsContract.CommonDataKinds.Website.TYPE,
                                if (work) ContactsContract.CommonDataKinds.Website.TYPE_WORK else ContactsContract.CommonDataKinds.Website.TYPE_HOME)
                        .build())
            }
        }
    }
}