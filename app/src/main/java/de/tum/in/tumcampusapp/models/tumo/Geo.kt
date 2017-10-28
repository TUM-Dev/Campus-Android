package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "geo")
data class Geo(@field:Element(required = false) var latitude: String = "0",
               @field:Element(required = false) var longitude: String = "0") {

    constructor(latitude: Double, longitude: Double) : this(
            latitude = java.lang.Double.toString(latitude),
            longitude = java.lang.Double.toString(longitude)
    )

    override fun toString(): String = "latitude=$latitude longitude=$longitude"
}