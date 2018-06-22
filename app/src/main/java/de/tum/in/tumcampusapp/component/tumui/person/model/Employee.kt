package de.tum.`in`.tumcampusapp.component.tumui.person.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.common.base.Charsets
import com.tickaroo.tikxml.annotation.Attribute
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
data class Employee(@Attribute(name = "geschlecht")
                    var gender: String = "",
                    @Attribute(name = "obfuscated_id")
                    var id: String = "",
                    @Attribute(name = "vorname")
                    var name: String = "",
                    @Attribute(name = "familienname")
                    var surname: String = "",
                    @Attribute(name = "dienstlich")
                    var businessContact: Contact? = null,
                    @Attribute(name = "sprechstunde")
                    var consultationHours: String = "",
                    @Attribute
                    var email: String = "",
                    @Attribute(name = "gruppen")
                    var groupList: GroupList? = null,
                    @Attribute(name = "image_data")
                    var imageData: String = "",
                    @Attribute(name = "privat")
                    var privateContact: Contact? = null,
                    @Attribute(name = "raeume")
                    var roomList: RoomList? = null,
                    @Attribute(name = "telefon_nebenstellen")
                    var telSubstationList: TelSubstationList? = null,
                    @Attribute(name = "titel")
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
