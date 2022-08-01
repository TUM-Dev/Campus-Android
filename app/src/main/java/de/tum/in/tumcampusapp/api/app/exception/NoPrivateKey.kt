package de.tum.`in`.tumcampusapp.api.app.exception

import java.lang.Exception

/**
 * No Public key could be found or has not been yet generated
 */
class NoPrivateKey : Exception() {
    private val serialVersionUID = -5945773588767314803L
}