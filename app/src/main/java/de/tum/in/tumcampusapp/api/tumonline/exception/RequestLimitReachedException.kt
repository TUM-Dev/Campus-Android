package de.tum.`in`.tumcampusapp.api.tumonline.exception

class RequestLimitReachedException : LimitReachedException(124) {

    override val message: String?
        get() = "The user has made to many requests to TUMonline. Try again later."

}