package de.tum.in.tumcampusapp.auxiliary;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Suggestion provider for {@link de.tum.in.tumcampusapp.activities.RoomFinderActivity}
 */
public class RoomFinderSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.auxiliary.RoomFinderSuggestionProvider";

    public RoomFinderSuggestionProvider() {
        setupSuggestions(AUTHORITY, 1);
    }
}