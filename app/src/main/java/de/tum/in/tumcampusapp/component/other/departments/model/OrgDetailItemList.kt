package de.tum.`in`.tumcampusapp.component.other.departments.model

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * This class is dealing with the de-serialization of the output of TUMOnline to the method "sucheLehrveranstaltungen".
 */

@Root(name = "rowset")
data class OrgDetailItemList(
        /**
         * List of OrganisationItems
         */
        @field:ElementList(inline = true, required = false)
        var groups: List<OrgDetailsItem> = mutableListOf())
