package de.tum.`in`.tumcampusapp.api.tumonline.exception

import java.io.InterruptedIOException

class InactiveTokenException : InterruptedIOException() {

    override val message: String?
        get() = "The user’s access token is inactive"

}