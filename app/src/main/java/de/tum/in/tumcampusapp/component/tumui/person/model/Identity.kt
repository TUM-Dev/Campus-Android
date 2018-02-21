package de.tum.`in`.tumcampusapp.component.tumui.person.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "row")
data class Identity(@field:Element var vorname: String = "",
                    @field:Element var familienname: String = "",
                    @field:Element var kennung: String = "",
                    @field:Element var obfuscated_id: String = "",
                    @field:Element var obfuscated_ids: ObfuscatedIds = ObfuscatedIds()) {
    override fun toString(): String = "$vorname $familienname"
}
