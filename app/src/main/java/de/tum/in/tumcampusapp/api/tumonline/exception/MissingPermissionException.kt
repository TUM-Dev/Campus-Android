package de.tum.`in`.tumcampusapp.api.tumonline.exception

import java.io.InterruptedIOException

class MissingPermissionException : InterruptedIOException() {

    override val message: String?
        get() = "Token is active but specific permission not given"

}