package de.tum.`in`.tumcampusapp.api.tumonline

enum class CacheControl(val header: String) {
    NO_CACHE("no-cache"),
    USE_CACHE("public")
}