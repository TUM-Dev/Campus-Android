package de.tum.in.tumcampusapp.auxiliary;

/**
 * Suggestion provider for {@link de.tum.in.tumcampusapp.activities.FacilityCategoriesActivity}
 */
public class FacilityLocatorSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampusapp.auxiliary.FacilityLocatorSuggestionProvider";

	public FacilityLocatorSuggestionProvider() {
		setupSuggestions("facilities", AUTHORITY);
	}
}