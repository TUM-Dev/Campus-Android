package de.tum.in.tumcampus.auxiliary;

public class PersonSearchSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.PersonSearchSuggestionProvider";

	public PersonSearchSuggestionProvider() {
		setupSuggestions("persons", AUTHORITY, DATABASE_MODE_QUERIES);
	}
}