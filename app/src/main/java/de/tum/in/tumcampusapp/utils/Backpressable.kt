package de.tum.`in`.tumcampusapp.utils

interface Backpressable {
    /**
     * Used to allow Fragments to respond to back-button events.
     *
     * @return True if back button action was handled, otherwise false
     */
    fun onBackPressed(): Boolean
}
