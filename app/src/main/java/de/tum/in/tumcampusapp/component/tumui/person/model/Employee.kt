package de.tum.`in`.tumcampusapp.component.tumui.person.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.tickaroo.tikxml.annotation.Element
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
                    @Element(name = "dienstlich")
                    val businessContact: Contact? = null,
                    @PropertyElement(name = "sprechstunde")
                    val consultationHours: String = "",
                    @PropertyElement
                    val email: String = "",
                    @Element(name = "gruppen")
                    val groupList: GroupList? = null,
                    @PropertyElement(name = "image_data")
                    val imageData: String = "",
                    @Element(name = "privat")
                    val privateContact: Contact? = null,
                    @Element(name = "raeume")
                    val roomList: RoomList? = null,
                    @Element(name = "telefon_nebenstellen")
                    val telSubstationList: TelSubstationList? = null,
                    @PropertyElement(name = "titel")
                    val title: String = "") {

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

        if (title.isBlank()) {
            return salutationWithName
        } else {
            return "$salutationWithName, $title"
        }
    }

}
