package de.tum.`in`.tumcampusapp.utils

object ErrorHelper {

    @JvmStatic
    fun crashOnException(throwable: Throwable) {
        throw throwable
    }

    @JvmStatic
    fun logAndIgnore(throwable: Throwable) {
        Utils.log(throwable)
    }

}
