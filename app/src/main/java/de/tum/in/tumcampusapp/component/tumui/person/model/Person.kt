package de.tum.`in`.tumcampusapp.component.tumui.person.model

import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.io.Serializable

/**
 * A person, often an [Employee] working at TUM.
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "row", strict = false)
data class Person(@field:Element(name = "geschlecht", required = false)
                  var gender: String = "",
                  @field:Element(name = "obfuscated_id")
                  var id: String = "",
                  @field:Element(name = "vorname")
                  var name: String = "",
                  @field:Element(name = "familienname")
                  var surname: String = "") :
        Serializable {

    override fun toString() = "$name $surname"

    companion object {
        val FEMALE = "W"
        val MALE = "M"
        private const val serialVersionUID = -5210814076506102292L

        fun fromRecent(r: Recent): Person {
            val split = r.name.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val p = Person()
            p.id = split[0]
            p.name = split[1]
            return p
        }
    }
}
