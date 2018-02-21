package de.tum.`in`.tumcampusapp.component.tumui.person.model

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "rowset")
data class IdentitySet(@field:ElementList(inline = true)
                       var ids: List<Identity> = mutableListOf()) {

    override fun toString(): String =
            if (ids.isEmpty())
                "null"
            else ids[0].toString()
}
