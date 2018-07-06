package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml
import java.io.Serializable

/**
 * Wrapper class holding a list of groups of an employee. Note: This model is
 * based on the TUMOnline web service response format for a corresponding
 * request.
 */

@Xml(name = "gruppen")
data class GroupList(@Element var groups: List<Group>? = null) : Serializable {
    companion object {
        private const val serialVersionUID = 2713473533623487005L
    }
}
