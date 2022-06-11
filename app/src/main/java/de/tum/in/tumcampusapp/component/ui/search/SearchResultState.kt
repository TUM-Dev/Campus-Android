package de.tum.`in`.tumcampusapp.component.ui.search

data class SearchResultState(
    val isLoading: Boolean = false,
    val data: List<SearchResult> = emptyList(),
    val availableResultTypes: List<SearchResultType> = emptyList(),
    val selectedType: SearchResultType = SearchResultType.ALL
)

