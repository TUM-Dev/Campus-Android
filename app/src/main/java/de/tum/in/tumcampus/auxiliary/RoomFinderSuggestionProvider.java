package de.tum.in.tumcampus.auxiliary;

public class RoomFinderSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.RoomFinderSuggestionProvider";

	public RoomFinderSuggestionProvider() {
		setupSuggestions("rooms", AUTHORITY, DATABASE_MODE_QUERIES);
	}
}