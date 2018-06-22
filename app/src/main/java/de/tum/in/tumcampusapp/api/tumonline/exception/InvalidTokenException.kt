package de.tum.`in`.tumcampusapp.api.tumonline.exception

import java.io.IOException

class InvalidTokenException : IOException() {

    override val message: String?
        get() = "The user’s token is not confirmed or invalid"

}