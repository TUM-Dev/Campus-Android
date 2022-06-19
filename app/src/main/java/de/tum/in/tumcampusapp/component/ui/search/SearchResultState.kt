package de.tum.`in`.tumcampusapp.component.ui.search

import de.tum.`in`.tumcampusapp.component.other.general.model.Recent

data class SearchResultState(
    val isLoading: Boolean = false,
    val data: List<SearchResult> = emptyList(),
    val availableResultTypes: List<SearchResultType> = emptyList(),
    val selectedType: SearchResultType = SearchResultType.ALL,
    val recentSearches: List<Recent> = emptyList()
)

