package de.tum.in.tumcampusapp.component.lectures;

import android.content.SearchRecentSuggestionsProvider;

import de.tum.in.tumcampusapp.component.lectures.activity.LecturesPersonalActivity;

/**
 * Suggestion provider for {@link LecturesPersonalActivity}
 */
public class LectureSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.component.lectures.LectureSearchSuggestionProvider";

    public LectureSearchSuggestionProvider() { setupSuggestions(AUTHORITY, 1); }
}