package de.tum.in.tumcampus.auxiliary;

import android.content.SearchRecentSuggestionsProvider;

public class RoomFinderSuggestionProvider extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.RoomFinderSuggestionProvider";

	public RoomFinderSuggestionProvider() {
		setupSuggestions(AUTHORITY, DATABASE_MODE_QUERIES);
	}
}