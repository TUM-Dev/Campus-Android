package de.tum.`in`.tumcampusapp.api.tumonline.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "error")
data class Error(@PropertyElement var message: String = "")
