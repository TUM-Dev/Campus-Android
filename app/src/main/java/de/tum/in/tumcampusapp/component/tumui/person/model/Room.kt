package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

import java.io.Serializable

/**
 * A room that belongs to some [Person] or [Employee]. Note: This
 * model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Xml(name = "raum")
data class Room(@PropertyElement(name = "ortsbeschreibung")
                var location: String = "",
                @PropertyElement(name = "kurz")
                var number: String = "") : Serializable {

    fun getFullLocation() = "$location ($number)"

    companion object {
        private const val serialVersionUID = -5328581629897735774L
    }

}
