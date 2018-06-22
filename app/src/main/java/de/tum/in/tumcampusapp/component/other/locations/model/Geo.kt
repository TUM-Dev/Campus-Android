package de.tum.`in`.tumcampusapp.component.other.locations.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "geo")
data class Geo(@Attribute var latitude: String = "0",
               @Attribute var longitude: String = "0") {

    constructor(latitude: Double, longitude: Double) : this(
            latitude = java.lang.Double.toString(latitude),
            longitude = java.lang.Double.toString(longitude)
    )

    override fun toString(): String = "latitude=$latitude longitude=$longitude"
}