package de.tum.in.tumcampusapp.component.transportation;

import android.content.SearchRecentSuggestionsProvider;

public class MVVStationSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.component.transportation.MVVStationSuggestionProvider";

    public MVVStationSuggestionProvider() {
        setupSuggestions(AUTHORITY, 1);
    }
}