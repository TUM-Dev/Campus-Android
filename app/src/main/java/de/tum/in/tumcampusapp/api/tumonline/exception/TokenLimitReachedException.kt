package de.tum.`in`.tumcampusapp.api.tumonline.exception

import java.io.InterruptedIOException

class TokenLimitReachedException : InterruptedIOException() {

    override val message: String?
        get() = "The user reached the limited of 10 active tokens"

}