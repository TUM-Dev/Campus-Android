package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.google.gson.Gson
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import java.io.Serializable

/**
 * A person, often an [Employee] working at TUM.
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Xml(name = "row")
data class Person(
    @PropertyElement(name = "geschlecht")
    var gender: String = "",
    @PropertyElement(name = "obfuscated_id")
    var id: String = "",
    @PropertyElement(name = "vorname")
    var name: String = "",
    @PropertyElement(name = "familienname")
    var surname: String = "",
    @PropertyElement(name = "bild_url")
    var imageUrl: String = "",
) : Serializable {

    fun getFullName() = "$name $surname"

    fun getFullImageUrl() =
            "https://campus.tum.de/tumonline/$imageUrl".replace("&amp;", "&")

    companion object {

        private const val serialVersionUID = -5210814076506102292L

        const val FEMALE = "W"
        const val MALE = "M"

        @JvmStatic fun toRecent(person: Person) : Recent {
            val gson = Gson()
            val jsonString = gson.toJson(person)
            return Recent(name = jsonString, type = RecentsDao.PERSONS)
        }

        @JvmStatic fun fromRecent(r: Recent): Person {
            val gson = Gson()
            return gson.fromJson(r.name, Person::class.java)
        }
    }
}
