package de.tum.`in`.tumcampusapp.component.tumui.person.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "row")
data class ObfuscatedIds(@field:Element(required = false) var studierende: String = "",
                         @field:Element(required = false) var bedienstete: String = "",
                         @field:Element(required = false) var extern: String = "")