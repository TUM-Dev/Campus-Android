package de.tum.`in`.tumcampusapp.api.tumonline.model

import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Root(name = "confirmed")
data class TokenConfirmation(@field:Text var confirmed: String = "false") {
    val isConfirmed: Boolean
        get() = "true" == confirmed
}
