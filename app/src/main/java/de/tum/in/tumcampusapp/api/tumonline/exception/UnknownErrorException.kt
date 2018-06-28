package de.tum.`in`.tumcampusapp.api.tumonline.exception

import java.io.IOException

class UnknownErrorException : IOException() {

    override val message: String?
        get() = "Unknown Exception..."

}