package de.tum.`in`.tumcampusapp.utils

/**
 * Executes the block and return null in case of an [Exception].
 *
 * @param block The block of code to execute
 */
inline fun <T> tryOrNull(block: () -> T): T? {
    return try {
        block()
    } catch (_: Exception) {
        null
    }
}