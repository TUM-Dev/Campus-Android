package de.tum.in.tumcampusapp.auxiliary;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Suggestion provider for {@link de.tum.in.tumcampusapp.activities.PersonsSearchActivity}
 */
public class PersonSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.auxiliary.PersonSearchSuggestionProvider";

    public PersonSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, 1);
    }
}