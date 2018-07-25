package de.tum.`in`.tumcampusapp.api.tumonline.exception

import java.io.InterruptedIOException

class InvalidTokenException : InterruptedIOException() {

    override val message: String?
        get() = "The user’s token is not confirmed or invalid"

}