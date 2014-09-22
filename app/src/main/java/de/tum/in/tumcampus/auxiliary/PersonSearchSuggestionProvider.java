package de.tum.in.tumcampus.auxiliary;

/**
 * Suggestion provider for {@link de.tum.in.tumcampus.activities.PersonsSearchActivity}
 */
public class PersonSearchSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.PersonSearchSuggestionProvider";

	public PersonSearchSuggestionProvider() {
		setupSuggestions("persons", AUTHORITY);
	}
}