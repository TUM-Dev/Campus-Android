package de.tum.in.tumcampus.auxiliary;

/**
 * Suggestion provider for {@link de.tum.in.tumcampus.activities.RoomFinderActivity}
 */
public class RoomFinderSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.RoomFinderSuggestionProvider";

	public RoomFinderSuggestionProvider() {
		setupSuggestions("rooms", AUTHORITY);
	}
}