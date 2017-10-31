package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

import java.io.Serializable

/**
 * Wrapper class holding a list of [TelSubstation]s. Note: This model is
 * based on the TUMOnline web service response format for a corresponding
 * request.
 */
@Root(name = "telefon_nebenstellen")
data class TelSubstationList(@field:ElementList(inline = true, required = false)
                             var substations: List<TelSubstation> = mutableListOf()) : Serializable {
    companion object {
        private const val serialVersionUID = -3790189526859194869L
    }
}
