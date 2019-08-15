package de.tum.`in`.tumcampusapp.component.tumui.person

import android.content.SearchRecentSuggestionsProvider

/**
 * Suggestion provider for [PersonSearchActivity]
 */
class PersonSearchSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, 1)
    }

    companion object {
        const val AUTHORITY = "de.tum.in.tumcampusapp.component.tumui.person.PersonSearchSuggestionProvider"
    }
}