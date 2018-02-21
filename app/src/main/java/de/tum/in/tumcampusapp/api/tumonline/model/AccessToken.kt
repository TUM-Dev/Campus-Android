package de.tum.`in`.tumcampusapp.api.tumonline.model

import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Root(name = "token")
data class AccessToken(@field:Text var token: String = "")
