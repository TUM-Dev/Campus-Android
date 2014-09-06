package de.tum.in.tumcampus.auxiliary;

import android.content.SearchRecentSuggestionsProvider;

public class MVVStationSuggestionProvider extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.MVVStationSuggestionProvider";

	public MVVStationSuggestionProvider() {
		setupSuggestions(AUTHORITY, DATABASE_MODE_QUERIES);
	}
}