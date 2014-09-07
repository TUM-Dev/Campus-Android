package de.tum.in.tumcampus.auxiliary;

public class LectureSearchSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.LectureSearchSuggestionProvider";

	public LectureSearchSuggestionProvider() {
		setupSuggestions("lecture", AUTHORITY, DATABASE_MODE_QUERIES);
	}
}