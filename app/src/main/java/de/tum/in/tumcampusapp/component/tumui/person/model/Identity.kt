package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "row")
data class Identity(
    @PropertyElement(name = "vorname") val firstName: String = "",
    @PropertyElement(name = "familienname") val lastName: String = "",
    @PropertyElement(name = "kennung") val id: String = "",
    @PropertyElement val obfuscated_id: String = "",
    @Element val obfuscated_ids: ObfuscatedIds = ObfuscatedIds()
) {
    override fun toString(): String = "$firstName $lastName"
}
