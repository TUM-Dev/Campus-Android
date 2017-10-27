package de.tum.in.tumcampusapp.auxiliary;

public class MVVStationSuggestionProvider extends EnhancedSearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "de.tum.in.tumcampusapp.auxiliary.MVVStationSuggestionProvider";

    public MVVStationSuggestionProvider() {
        setupSuggestions("mvv", AUTHORITY);
    }
}