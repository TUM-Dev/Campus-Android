package de.tum.in.tumcampusapp.component.tumui.lectures;

import android.content.SearchRecentSuggestionsProvider;

public class LectureSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.component.tumui.lectures.LectureSearchSuggestionProvider";

    public LectureSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, 1);
    }
}