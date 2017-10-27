package de.tum.in.tumcampusapp.auxiliary;

/**
 * Suggestion provider for {@link de.tum.in.tumcampusapp.activities.PersonsSearchActivity}
 */
public class PersonSearchSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.auxiliary.PersonSearchSuggestionProvider";

    public PersonSearchSuggestionProvider() {
        setupSuggestions("persons", AUTHORITY);
    }
}