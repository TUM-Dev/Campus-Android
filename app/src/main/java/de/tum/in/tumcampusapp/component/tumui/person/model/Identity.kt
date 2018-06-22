package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "row")
data class Identity(@PropertyElement(name = "vorname") var firstName: String = "",
                    @PropertyElement(name = "familienname") var lastName: String = "",
                    @PropertyElement(name = "kennung") var id: String = "",
                    @PropertyElement var obfuscated_id: String = "",
                    @PropertyElement var obfuscated_ids: ObfuscatedIds = ObfuscatedIds()) {
    override fun toString(): String = "$firstName $lastName"
}
