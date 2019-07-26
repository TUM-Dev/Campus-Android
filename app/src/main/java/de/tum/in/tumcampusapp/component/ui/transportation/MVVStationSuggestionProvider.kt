package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.content.SearchRecentSuggestionsProvider

class MVVStationSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, 1)
    }

    companion object {
        const val AUTHORITY = "de.tum.in.tumcampusapp.component.ui.transportation.MVVStationSuggestionProvider"
    }
}