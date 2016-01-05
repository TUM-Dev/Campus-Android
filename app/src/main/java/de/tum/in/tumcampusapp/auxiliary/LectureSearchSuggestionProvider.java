package de.tum.in.tumcampusapp.auxiliary;

/**
 * Suggestion provider for {@link de.tum.in.tumcampusapp.activities.LecturesPersonalActivity}
 */
public class LectureSearchSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.LectureSearchSuggestionProvider";

	public LectureSearchSuggestionProvider() {
		setupSuggestions("lecture", AUTHORITY);
	}
}