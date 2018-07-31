package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "termin")
data class DeleteEventResponse(@PropertyElement(name = "delete") val delete: String = "")