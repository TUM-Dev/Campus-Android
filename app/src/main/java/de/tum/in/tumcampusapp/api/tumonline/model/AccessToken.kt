package de.tum.`in`.tumcampusapp.api.tumonline.model

import com.tickaroo.tikxml.annotation.TextContent
import com.tickaroo.tikxml.annotation.Xml

@Xml
data class AccessToken(@TextContent val token: String)
