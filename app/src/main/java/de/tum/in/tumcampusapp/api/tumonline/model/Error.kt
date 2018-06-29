package de.tum.`in`.tumcampusapp.api.tumonline.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.api.tumonline.exception.*
import java.io.IOException

@Xml(name = "error")
data class Error(@PropertyElement var message: String = "") {

    val exception: IOException
        get() = ERROR_MESSAGE_TO_EXCEPTION.entries
                .filter { message.contains(it.key) }
                .map { it.value }
                .firstOrNull() ?: UnknownErrorException()

    companion object {

        private val ERROR_MESSAGE_TO_EXCEPTION = mapOf(
                "Keine Rechte für Funktion" to MissingPermissionException(),
                "Token ist ungültig!" to InvalidTokenException(),
                "ungültiges Benutzertoken" to InvalidTokenException(),
                "Token ist nicht bestätigt!" to InactiveTokenException(),
                "Request-Rate überschritten" to RequestLimitReachedException(),
                "Token-Limit" to TokenLimitReachedException()
        )

    }

}
