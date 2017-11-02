package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "row")
data class ObfuscatedIds(@field:Element var studierende: String = "",
                         @field:Element var bedienstete: String = "",
                         @field:Element var extern: String = "") {

}