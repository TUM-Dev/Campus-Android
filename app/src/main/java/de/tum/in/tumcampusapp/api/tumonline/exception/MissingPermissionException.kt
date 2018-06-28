package de.tum.`in`.tumcampusapp.api.tumonline.exception

import java.io.IOException

class MissingPermissionException : IOException() {

    override val message: String?
        get() = "Token is active but specific permission not given"

}