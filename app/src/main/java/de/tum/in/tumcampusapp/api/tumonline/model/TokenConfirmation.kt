package de.tum.`in`.tumcampusapp.api.tumonline.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "confirmed")
data class TokenConfirmation(@PropertyElement var confirmed: String = "false") {
    val isConfirmed: Boolean
        get() = "true" == confirmed
}
