package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

import java.io.Serializable

/**
 * Wrapper class holding a list of [TelSubstation]s. Note: This model is
 * based on the TUMOnline web service response format for a corresponding
 * request.
 */
@Xml(name = "telefon_nebenstellen")
data class TelSubstationList(@Element var substations: List<TelSubstation>? = null) : Serializable {
    companion object {
        private const val serialVersionUID = -3790189526859194869L
    }
}
