package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "termin")
data class CreateEvent (@Attribute(name = "nr") var eventNr: String = "")
