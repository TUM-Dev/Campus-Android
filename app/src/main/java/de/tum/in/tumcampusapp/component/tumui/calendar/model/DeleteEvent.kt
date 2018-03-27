package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "termin")
data class DeleteEvent(@field:Element(name = "delete") var delete: String = "")