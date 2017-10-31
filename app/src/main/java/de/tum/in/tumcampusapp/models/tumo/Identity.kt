package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "row")
data class Identity(@field:Element var vorname: String = "",
                    @field:Element var familienname: String = "",
                    @field:Element var kennung: String = "") {
    override fun toString(): String = "$vorname $familienname"
}
