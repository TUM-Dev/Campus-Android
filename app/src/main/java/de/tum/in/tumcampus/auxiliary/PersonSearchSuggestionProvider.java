package de.tum.in.tumcampus.auxiliary;

import android.content.SearchRecentSuggestionsProvider;

public class PersonSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.PersonSearchSuggestionProvider";

	public PersonSearchSuggestionProvider() {
		setupSuggestions(AUTHORITY, DATABASE_MODE_QUERIES);
	}
}