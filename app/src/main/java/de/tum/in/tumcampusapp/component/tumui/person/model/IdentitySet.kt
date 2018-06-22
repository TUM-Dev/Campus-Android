package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "rowset")
data class IdentitySet(@Element val ids: List<Identity>) {

    override fun toString(): String =
            if (ids.isEmpty())
                "null"
            else ids[0].toString()

}
