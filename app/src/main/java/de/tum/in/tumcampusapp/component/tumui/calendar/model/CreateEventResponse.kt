package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "termin")
data class CreateEventResponse(@PropertyElement(name = "nr") val eventId: String = "")
