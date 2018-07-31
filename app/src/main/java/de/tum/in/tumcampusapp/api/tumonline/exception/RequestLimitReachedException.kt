package de.tum.`in`.tumcampusapp.api.tumonline.exception

import java.io.InterruptedIOException

class RequestLimitReachedException : InterruptedIOException() {

    override val message: String?
        get() = "The user has made to many requests to TUMonline. Try again later."

}