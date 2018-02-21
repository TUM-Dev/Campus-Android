package de.tum.in.tumcampusapp.auxiliary;

import android.content.SearchRecentSuggestionsProvider;

import de.tum.in.tumcampusapp.component.person.activity.PersonsSearchActivity;

/**
 * Suggestion provider for {@link PersonsSearchActivity}
 */
public class PersonSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.auxiliary.PersonSearchSuggestionProvider";

    public PersonSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, 1);
    }
}