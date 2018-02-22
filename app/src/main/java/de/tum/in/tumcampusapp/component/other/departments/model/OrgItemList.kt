package de.tum.`in`.tumcampusapp.component.other.departments.model

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * This class is dealing with the de-serialization of the output of TUMOnline to the method "sucheLehrveranstaltungen".
 */

@Root(name = "rowset")
data class OrgItemList(
        @field:ElementList(inline = true, required = false)
        var groups: List<OrgItem> = mutableListOf())