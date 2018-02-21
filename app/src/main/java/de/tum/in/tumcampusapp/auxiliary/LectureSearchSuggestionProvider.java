package de.tum.in.tumcampusapp.auxiliary;

import android.content.SearchRecentSuggestionsProvider;

import de.tum.in.tumcampusapp.component.lecture.activity.LecturesPersonalActivity;

/**
 * Suggestion provider for {@link LecturesPersonalActivity}
 */
public class LectureSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.auxiliary.LectureSearchSuggestionProvider";

    public LectureSearchSuggestionProvider() { setupSuggestions(AUTHORITY, 1); }
}