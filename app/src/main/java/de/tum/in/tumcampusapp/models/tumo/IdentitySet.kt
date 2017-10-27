package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "rowset")
data class IdentitySet(@field:ElementList(inline = true)
                       var ids: List<Identity> = emptyList()) {

    override fun toString(): String {
        return if (ids.isEmpty())
            "null"
        else ids[0].toString()
    }
}
