package de.tum.in.tumcampusapp.component.person;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Suggestion provider for {@link PersonsSearchActivity}
 */
public class PersonSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.component.person.PersonSearchSuggestionProvider";

    public PersonSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, 1);
    }
}