package de.tum.`in`.tumcampusapp.api.tumonline.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "confirmed")
data class TokenConfirmation(@Attribute var confirmed: String = "false") {
    val isConfirmed: Boolean
        get() = "true" == confirmed
}
