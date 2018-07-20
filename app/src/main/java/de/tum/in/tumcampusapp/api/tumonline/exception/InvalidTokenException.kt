package de.tum.`in`.tumcampusapp.api.tumonline.exception

class InvalidTokenException : TokenException(22) {

    override val message: String?
        get() = "The user’s token is not confirmed or invalid"

}