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
                    val gender: String? = null,
                    @PropertyElement(name = "obfuscated_id")
                    val id: String = "",
                    @PropertyElement(name = "vorname")
                    val name: String = "",
                    @PropertyElement(name = "familienname")
                    val surname: String = "",
                    @PropertyElement(name = "dienstlich")
                    val businessContact: Contact? = null,
                    @PropertyElement(name = "sprechstunde")
                    val consultationHours: String? = null,
                    @PropertyElement
                    val email: String? = null,
                    @PropertyElement(name = "gruppen")
                    val groupList: GroupList? = null,
                    @PropertyElement(name = "image_data")
                    val imageData: String? = null,
                    @PropertyElement(name = "privat")
                    val privateContact: Contact? = null,
                    @PropertyElement(name = "raeume")
                    val roomList: RoomList? = null,
                    @PropertyElement(name = "telefon_nebenstellen")
                    val telSubstationList: TelSubstationList? = null,
                    @PropertyElement(name = "titel")
                    val title: String? = null) {

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
        return arrayOf(title, name, surname)
                .filterNotNull()
                .joinToString(" ")
    }

    companion object {
        private val serialVersionUID = -6276330922677632119L
    }
}
