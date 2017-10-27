package de.tum.in.tumcampusapp.auxiliary;

/**
 * Suggestion provider for {@link de.tum.in.tumcampusapp.activities.RoomFinderActivity}
 */
public class RoomFinderSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.auxiliary.RoomFinderSuggestionProvider";

    public RoomFinderSuggestionProvider() {
        setupSuggestions("rooms", AUTHORITY);
    }
}