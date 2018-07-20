package de.tum.`in`.tumcampusapp.api.tumonline.exception

class InactiveTokenException : TokenException(44) {

    override val message: String?
        get() = "The user’s access token is inactive"

}