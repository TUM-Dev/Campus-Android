package de.tum.in.tumcampusapp.auxiliary;

import de.tum.in.tumcampusapp.activities.FacilityMainActivity;

/**
 * Suggestion provider for {@link FacilityMainActivity}
 */
public class FacilityLocatorSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampusapp.auxiliary.FacilityLocatorSuggestionProvider";

	public FacilityLocatorSuggestionProvider() {
		setupSuggestions("facilities", AUTHORITY);
	}
}