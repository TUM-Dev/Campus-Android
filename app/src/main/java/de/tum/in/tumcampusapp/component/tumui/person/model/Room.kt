package de.tum.`in`.tumcampusapp.component.tumui.person.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

import java.io.Serializable

/**
 * A room that belongs to some [Person] or [Employee]. Note: This
 * model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "raum", strict = false)
data class Room(@field:Element(name = "ortsbeschreibung")
                var location: String = "",
                @field:Element(name = "kurz")
                var number: String = "") :
        Serializable {
    companion object {
        private const val serialVersionUID = -5328581629897735774L
    }
}
