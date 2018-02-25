package de.tum.in.tumcampusapp.component.tumui.lectures;

import android.content.SearchRecentSuggestionsProvider;

import de.tum.in.tumcampusapp.component.tumui.lectures.activity.LecturesPersonalActivity;

/**
 * Suggestion provider for {@link LecturesPersonalActivity}
 */
public class LectureSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.component.tumui.lectures.LectureSearchSuggestionProvider";

    public LectureSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, 1);
    }
}