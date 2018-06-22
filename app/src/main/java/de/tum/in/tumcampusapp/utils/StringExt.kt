package de.tum.`in`.tumcampusapp.utils

fun String.compareTo(other: String?): Int {
    return if (other != null) {
        compareTo(other)
    } else {
        -1
    }
}