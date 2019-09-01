package de.tum.`in`.tumcampusapp.api.tumonline.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.api.tumonline.exception.*
import java.io.InterruptedIOException

@Xml(name = "error")
data class Error(@PropertyElement var message: String = "") {

    val exception: InterruptedIOException
        get() = errorMessageToException
                .filter { message.contains(it.first) }
                .map { it.second }
                .firstOrNull() ?: UnknownErrorException()

    companion object {

        private val errorMessageToException = listOf(
                Pair("Keine Rechte für Funktion", MissingPermissionException()),
                Pair("Token ist ungültig!", InvalidTokenException()),
                Pair("ungültiges Benutzertoken", InvalidTokenException()),
                Pair("Token ist nicht bestätigt!", InactiveTokenException()),
                Pair("Request-Rate überschritten", RequestLimitReachedException()),
                Pair("Token-Limit", TokenLimitReachedException())
        )
    }
}
