package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "termin")
data class DeleteEvent(@PropertyElement(name = "delete") var delete: String = "")