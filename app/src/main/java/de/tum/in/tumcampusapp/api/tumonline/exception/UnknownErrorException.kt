package de.tum.`in`.tumcampusapp.api.tumonline.exception

import java.io.InterruptedIOException

class UnknownErrorException : InterruptedIOException() {

    override val message: String?
        get() = "Unknown Exception..."
}