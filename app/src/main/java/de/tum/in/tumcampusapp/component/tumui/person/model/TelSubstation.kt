package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml
import java.io.Serializable

/**
 * Telefon substation to reach an employee. Note: This model is based on the
 * TUMOnline web service response format for a corresponding request.
 */
@Xml(name = "nebenstelle")
data class TelSubstation(@Attribute(name = "telefonnummer")
                         var number: String = "") : Serializable {
    companion object {
        private const val serialVersionUID = -3289209179488515142L
    }
}
