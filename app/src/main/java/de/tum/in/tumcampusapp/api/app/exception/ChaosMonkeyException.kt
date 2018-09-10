package de.tum.`in`.tumcampusapp.api.app.exception

import java.io.InterruptedIOException

/**
 * Exception for resilience testing your network error handling
 * If this exception crashes your app, you should feel bad and implement proper error handling
 */
class ChaosMonkeyException(url: String) : InterruptedIOException("Some requests might spontaneously fail ($url)")