package de.tum.`in`.tumcampusapp.api.app.exception

import java.io.InterruptedIOException

class NoNetworkConnectionException : InterruptedIOException() {

    override val message: String?
        get() = "Device is not connected to the Internet"

}