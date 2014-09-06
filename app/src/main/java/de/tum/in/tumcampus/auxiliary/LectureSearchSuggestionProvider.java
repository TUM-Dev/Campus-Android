package de.tum.in.tumcampus.auxiliary;

import android.content.SearchRecentSuggestionsProvider;

public class LectureSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.LectureSearchSuggestionProvider";

	public LectureSearchSuggestionProvider() {
		setupSuggestions(AUTHORITY, DATABASE_MODE_QUERIES);
	}
}