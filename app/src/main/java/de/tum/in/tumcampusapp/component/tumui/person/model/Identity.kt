package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "row")
data class Identity(@PropertyElement var vorname: String = "",
                    @PropertyElement var familienname: String = "",
                    @PropertyElement var kennung: String = "",
                    @PropertyElement var obfuscated_id: String = "",
                    @PropertyElement var obfuscated_ids: ObfuscatedIds = ObfuscatedIds()) {
    override fun toString(): String = "$vorname $familienname"
}
