package de.tum.in.tumcampusapp.auxiliary;

import android.content.SearchRecentSuggestionsProvider;

public class MVVStationSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.auxiliary.MVVStationSuggestionProvider";

    public MVVStationSuggestionProvider() {
        setupSuggestions(AUTHORITY, 1);
    }
}