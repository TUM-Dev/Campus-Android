package de.tum.in.tumcampusapp.component.tumui.roomfinder;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Suggestion provider for {@link RoomFinderFragment}
 */
public class RoomFinderSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.component.tumui.roomfinder.RoomFinderSuggestionProvider";

    public RoomFinderSuggestionProvider() {
        setupSuggestions(AUTHORITY, 1);
    }
}