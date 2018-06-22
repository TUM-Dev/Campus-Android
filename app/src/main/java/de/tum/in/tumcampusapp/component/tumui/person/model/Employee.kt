package de.tum.`in`.tumcampusapp.component.tumui.person.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.common.base.Charsets
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.R

/**
 * An employee of the TUM.
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Xml(name = "person")
data class Employee(@PropertyElement(name = "geschlecht")
                    var gender: String? = null,
                    @PropertyElement(name = "obfuscated_id")
                    var id: String = "",
                    @PropertyElement(name = "vorname")
                    var name: String = "",
                    @PropertyElement(name = "familienname")
                    var surname: String = "",
                    @PropertyElement(name = "dienstlich")
                    var businessContact: Contact? = null,
                    @PropertyElement(name = "sprechstunde")
                    var consultationHours: String? = null,
                    @PropertyElement
                    var email: String? = null,
                    @PropertyElement(name = "gruppen")
                    var groupList: GroupList? = null,
                    @PropertyElement(name = "image_data")
                    var imageData: String? = null,
                    @PropertyElement(name = "privat")
                    var privateContact: Contact? = null,
                    @PropertyElement(name = "raeume")
                    var roomList: RoomList? = null,
                    @PropertyElement(name = "telefon_nebenstellen")
                    var telSubstationList: TelSubstationList? = null,
                    @PropertyElement(name = "titel")
                    var title: String? = null) {

    val groups: List<Group>?
        get() = groupList?.groups

    val image: Bitmap?
        get() {
            val imageAsBytes = Base64.decode(imageData?.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
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

        if (title?.isEmpty() == true) {
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
