package de.tum.in.tumcampus.auxiliary;

/**
 * Suggestion provider for {@link de.tum.in.tumcampus.activities.LecturesPersonalActivity}
 */
public class LectureSearchSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.LectureSearchSuggestionProvider";

	public LectureSearchSuggestionProvider() {
		setupSuggestions("lecture", AUTHORITY);
	}
}