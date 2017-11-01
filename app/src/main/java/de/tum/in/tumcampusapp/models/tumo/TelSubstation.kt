package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

import java.io.Serializable

/**
 * Telefon substation to reach an employee. Note: This model is based on the
 * TUMOnline web service response format for a corresponding request.
 */
@Root(name = "nebenstelle", strict = false)
data class TelSubstation(@field:Element(name = "telefonnummer")
                         var number: String = "") : Serializable {
    companion object {
        private const val serialVersionUID = -3289209179488515142L
    }
}
