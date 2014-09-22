package de.tum.in.tumcampus.auxiliary;

public class MVVStationSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "de.tum.in.tumcampus.auxiliary.MVVStationSuggestionProvider";

	public MVVStationSuggestionProvider() {
		setupSuggestions("mvv", AUTHORITY);
	}
}