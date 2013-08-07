package de.tum.in.tumcampusapp.auxiliary;

import android.content.SearchRecentSuggestionsProvider;


public class RoomFinderSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.example.RoomFinderSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public RoomFinderSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}