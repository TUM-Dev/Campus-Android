package de.tum.`in`.tumcampusapp.api.tumonline.model

import com.tickaroo.tikxml.annotation.TextContent
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "confirmed")
data class TokenConfirmation(@TextContent val confirmed: String) {
    val isConfirmed: Boolean
        get() = "true" == confirmed
}
