package de.tum.`in`.tumcampusapp.utils

inline fun <T> tryOrNull(f: () -> T): T? {
    try {
        return f()
    } catch (_: Exception) {
        return null
    }
}