package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.content.SearchRecentSuggestionsProvider

/**
 * Suggestion provider for [RoomFinderActivity]
 */
class RoomFinderSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, 1)
    }

    companion object {
        const val AUTHORITY = "de.tum.in.tumcampusapp.component.tumui.roomfinder.RoomFinderSuggestionProvider"
    }
}