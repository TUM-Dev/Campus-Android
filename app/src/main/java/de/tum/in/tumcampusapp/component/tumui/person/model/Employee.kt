package de.tum.`in`.tumcampusapp.component.tumui.person.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

import com.google.common.base.Charsets
import de.tum.`in`.tumcampusapp.R

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * An employee of the TUM.
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "person", strict = false)
data class Employee(@field:Element(name = "geschlecht", required = false)
                    var gender: String = "",
                    @field:Element(name = "obfuscated_id")
                    var id: String = "",
                    @field:Element(name = "vorname")
                    var name: String = "",
                    @field:Element(name = "familienname")
                    var surname: String = "",
                    @field:Element(name = "dienstlich")
                    var businessContact: Contact? = null,
                    @field:Element(name = "sprechstunde", required = false)
                    var consultationHours: String = "",
                    @field:Element(required = false)
                    var email: String = "",
                    @field:Element(name = "gruppen", required = false)
                    var groupList: GroupList? = null,
                    @field:Element(name = "image_data", required = false)
                    var imageData: String = "",
                    @field:Element(name = "privat")
                    var privateContact: Contact? = null,
                    @field:Element(name = "raeume", required = false)
                    var roomList: RoomList? = null,
                    @field:Element(name = "telefon_nebenstellen", required = false)
                    var telSubstationList: TelSubstationList? = null,
                    @field:Element(name = "titel", required = false)
                    var title: String = "") {

    val groups: List<Group>?
        get() = groupList?.groups

    val image: Bitmap?
        get() {
            val imageAsBytes = Base64.decode(imageData.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
        }

    val rooms: List<Room>?
        get() = roomList?.rooms

    val telSubstations: List<TelSubstation>?
        get() = telSubstationList?.substations

    fun getNameWithTitle(context: Context): String {
        val resourceId = if (gender == Person.FEMALE) R.string.mrs else R.string.mr
        val salutation = context.getString(resourceId)
        val salutationWithName = "$salutation $name $surname"

        if (title.isEmpty()) {
            return salutationWithName
        } else {
            return "$salutationWithName, $title"
        }
    }

    override fun toString(): String {
        var infoText = title

        if (title != "") {
            infoText = title + ' '
        }

        return infoText + name + ' ' + surname
    }

    companion object {
        private val serialVersionUID = -6276330922677632119L
    }
}
