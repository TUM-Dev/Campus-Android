package de.tum.in.tumcampusapp.auxiliary;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Suggestion provider for {@link de.tum.in.tumcampusapp.activities.LecturesPersonalActivity}
 */
public class LectureSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.auxiliary.LectureSearchSuggestionProvider";

    public LectureSearchSuggestionProvider() { setupSuggestions(AUTHORITY, 1); }
}