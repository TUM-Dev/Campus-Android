package de.tum.in.tumcampus.auxiliary;

import android.content.SearchRecentSuggestionsProvider;

/**
 * RoomFinderSuggestionProvider
 * 
 * @author
 * 
 */
public class RoomFinderSuggestionProvider extends
		SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "com.example.RoomFinderSuggestionProvider";
	public final static int MODE = DATABASE_MODE_QUERIES;

	public RoomFinderSuggestionProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
}