package de.tum.`in`.tumcampusapp.api.tumonline.exception

import java.io.IOException

class InactiveTokenException : IOException() {

    override val message: String?
        get() = "The user’s access token is inactive"

}