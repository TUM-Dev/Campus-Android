package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "row")
data class Identity(@Attribute var vorname: String = "",
                    @Attribute var familienname: String = "",
                    @Attribute var kennung: String = "",
                    @Attribute var obfuscated_id: String = "",
                    @Attribute var obfuscated_ids: ObfuscatedIds = ObfuscatedIds()) {
    override fun toString(): String = "$vorname $familienname"
}
