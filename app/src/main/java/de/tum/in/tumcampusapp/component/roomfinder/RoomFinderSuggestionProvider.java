package de.tum.in.tumcampusapp.component.roomfinder;

import android.content.SearchRecentSuggestionsProvider;

import de.tum.in.tumcampusapp.component.roomfinder.activity.RoomFinderActivity;

/**
 * Suggestion provider for {@link RoomFinderActivity}
 */
public class RoomFinderSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.component.roomfinder.RoomFinderSuggestionProvider";

    public RoomFinderSuggestionProvider() {
        setupSuggestions(AUTHORITY, 1);
    }
}