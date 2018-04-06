package de.tum.`in`.tumcampusapp.api.tumonline.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "error", strict = false)
data class Error(@field:Element(name = "message", required = false) var message: String = "")
