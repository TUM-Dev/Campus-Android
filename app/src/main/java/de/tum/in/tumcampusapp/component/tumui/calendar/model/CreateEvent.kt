package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "termin")
data class CreateEvent (@field:Element(name = "nr") var eventNr: String = "")
