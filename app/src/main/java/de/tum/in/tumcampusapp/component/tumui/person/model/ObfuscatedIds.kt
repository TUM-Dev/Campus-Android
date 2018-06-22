package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "row")
data class ObfuscatedIds(@Attribute var studierende: String = "",
                         @Attribute var bedienstete: String = "",
                         @Attribute var extern: String = "")