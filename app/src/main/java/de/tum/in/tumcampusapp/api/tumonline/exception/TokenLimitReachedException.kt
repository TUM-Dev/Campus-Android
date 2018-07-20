package de.tum.`in`.tumcampusapp.api.tumonline.exception

class TokenLimitReachedException : LimitReachedException(123) {

    override val message: String?
        get() = "The user reached the limited of 10 active tokens"

}