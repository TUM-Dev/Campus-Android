package de.tum.`in`.tumcampusapp.component.tumui.person.model

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

import java.io.Serializable

/**
 * Wrapper class holding a list of groups of an employee. Note: This model is
 * based on the TUMOnline web service response format for a corresponding
 * request.
 */

@Root(name = "gruppen")
data class GroupList(@field:ElementList(inline = true, required = false)
                     var groups: List<Group> = mutableListOf()) : Serializable {
    companion object {
        private const val serialVersionUID = 2713473533623487005L
    }
}
