package de.tum.`in`.tumcampusapp.component.tumui.lectures

import android.content.SearchRecentSuggestionsProvider

import de.tum.`in`.tumcampusapp.component.tumui.lectures.activity.LecturesPersonalActivity

/**
 * Suggestion provider for [LecturesPersonalActivity]
 */
class LectureSearchSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, 1)
    }

    companion object {
        const val AUTHORITY = "de.tum.in.tumcampusapp.component.tumui.lectures.LectureSearchSuggestionProvider"
    }
}