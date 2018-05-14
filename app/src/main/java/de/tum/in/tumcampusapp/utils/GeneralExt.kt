package de.tum.`in`.tumcampusapp.utils

inline fun <T> tryOrNull(f: () -> T): T? {
    return try {
        f()
    } catch (_: Exception) {
        null
    }
}