package de.tum.`in`.tumcampusapp.component.ui.search

import android.content.Context
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.api.navigatum.NavigaTumAPIClient
import de.tum.`in`.tumcampusapp.api.navigatum.model.search.NavigaTumSearchResponseDto
import de.tum.`in`.tumcampusapp.api.navigatum.model.search.NavigaTumSearchSectionDto
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.IllegalStateException
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val tumOnlineClient: TUMOnlineClient,
    private val navigaTumAPIClient: NavigaTumAPIClient
) : ViewModel() {

    val state: MutableStateFlow<SearchResultState> = MutableStateFlow(SearchResultState())

    private val persons: MutableStateFlow<List<SearchResult>> = MutableStateFlow(emptyList())
    private val lectures: MutableStateFlow<List<SearchResult>> = MutableStateFlow(emptyList())
    private val navigaRooms: MutableStateFlow<List<SearchResult>> = MutableStateFlow(emptyList())
    private val buildings: MutableStateFlow<List<SearchResult>> = MutableStateFlow(emptyList())

    private val compositeDisposable = CompositeDisposable()

    private var currentApiCalls = 0
    private var currentQueryText = ""

    fun changeResultType(type: SearchResultType) {
        val selectedResult: List<SearchResult> = when (type) {
            SearchResultType.PERSON -> persons.value
            SearchResultType.LECTURE -> lectures.value
            SearchResultType.NAVIGA_ROOM -> navigaRooms.value
            SearchResultType.BUILDING -> buildings.value
            SearchResultType.ALL -> persons.value + buildings.value + navigaRooms.value + lectures.value
        }
        state.value = state.value.copy(
            data = selectedResult,
            selectedType = type
        )
    }

    private fun searchForBuildingAndRooms(query: String) {
        compositeDisposable += navigaTumAPIClient
            .searchSingle(query)
            .subscribeOn(Schedulers.io())
            .doOnError(Utils::log)
            .onErrorReturn { NavigaTumSearchResponseDto() }
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.sections }
            .subscribe { result ->
                if (result.isNotEmpty()) {
                    result.forEach { navigationDto ->
                        when (navigationDto.type) {
                            NavigaTumSearchSectionDto.BUILDINGS_TYPE -> {
                                val buildingSearchResultList = navigationDto.entries
                                    .map { SearchResult.Building(it) }
                                    .toList()
                                saveSearchResult(buildingSearchResultList, query)
                            }
                            NavigaTumSearchSectionDto.ROOMS -> {
                                val roomSearchResultList = navigationDto.entries
                                    .map { SearchResult.NavigaRoom(it) }
                                    .toList()
                                saveSearchResult(roomSearchResultList, query)
                            }
                        }
                    }
                } else currentApiCalls -= 2
            }
    }

    fun search(query: String) {
        compositeDisposable.clear()
        currentApiCalls = NUMBER_OF_API_CALLS
        currentQueryText = query
        persons.value = emptyList()
        lectures.value = emptyList()
        buildings.value = emptyList()
        navigaRooms.value = emptyList()
        state.value = state.value.copy(
            isLoading = true,
            data = emptyList(),
            availableResultTypes = emptySet(),
            selectedType = SearchResultType.ALL
        )

        val persons = tumOnlineClient
            .searchPerson(query)
            .subscribeOn(Schedulers.io())
            .map { it.persons }
            .doOnError(Utils::log)
            .onErrorReturn { emptyList() }
            .map { persons ->
                persons.map { SearchResult.Person(it) }
            }

        searchForBuildingAndRooms(query)

        val lectures = tumOnlineClient
            .searchLecturesSingle(query)
            .subscribeOn(Schedulers.io())
            .map { it.lectures }
            .doOnError(Utils::log)
            .onErrorReturn { emptyList() }
            .map { lectures ->
                lectures.map { SearchResult.Lecture(it) }
            }

        compositeDisposable += Single
            .concat(persons, lectures)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { saveSearchResult(it, query) }
    }

    private fun saveSearchResult(result: List<SearchResult>, query: String) {
        currentApiCalls -= 1
        if (query != currentQueryText) // don't save result if query already changed
            return

        if (result.isEmpty()) {
            saveResult(null)
            return
        }

        val type: SearchResultType = when (result[0]) {
            is SearchResult.Person -> SearchResultType.PERSON
            is SearchResult.Lecture -> SearchResultType.LECTURE
            is SearchResult.Building -> SearchResultType.BUILDING
            is SearchResult.NavigaRoom -> SearchResultType.NAVIGA_ROOM
        }

        when (type) {
            SearchResultType.PERSON -> persons.value = result
            SearchResultType.LECTURE -> lectures.value = result
            SearchResultType.BUILDING -> buildings.value = result
            SearchResultType.NAVIGA_ROOM -> navigaRooms.value = result
            else -> throw IllegalStateException("Not know search result type!")
        }
        saveResult(type)
    }

    private fun saveResult(type: SearchResultType?) {
        var availableTypes = state.value.availableResultTypes
        if (type != null)
            availableTypes = availableTypes + listOf(type)
        state.value = state.value.copy(
            isLoading = currentApiCalls > 0,
            data = persons.value + buildings.value + navigaRooms.value + lectures.value,
            availableResultTypes = availableTypes
        )
    }

    fun clearSearchState() {
        compositeDisposable.clear()
        state.value = state.value.copy(
            isLoading = false,
            data = emptyList(),
            availableResultTypes = emptySet(),
            selectedType = SearchResultType.ALL
        )
    }

    fun fetchRecentSearches(context: Context) {
        val recentSearchesDao: RecentsDao = TcaDb.getInstance(context).recentsDao()
        val recentSearches: List<Recent> = recentSearchesDao.allRecentSearches.reversed() ?: emptyList()
        state.value = state.value.copy(
            recentSearches = recentSearches.filter { x -> x.type != STATIONS } // filter for undesirable stations
        )
    }

    fun removeRecentSearch(recent: Recent, context: Context) {
        val recentSearchesDao: RecentsDao = TcaDb.getInstance(context).recentsDao()
        recentSearchesDao.deleteByName(recent.name)
        val recentSearches: List<Recent> = recentSearchesDao.allRecentSearches.reversed() ?: emptyList()
        state.value = state.value.copy(
            recentSearches = recentSearches
        )
    }

    fun clearRecentSearchesHistory(context: Context) {
        val recentSearchesDao: RecentsDao = TcaDb.getInstance(context).recentsDao()
        recentSearchesDao.removeCache()
        state.value = state.value.copy(
            recentSearches = emptyList()
        )
    }

    fun saveRecentSearch(recent: Recent, context: Context) {
        val recentSearchesDao: RecentsDao = TcaDb.getInstance(context).recentsDao()
        recentSearchesDao.insert(recent)
        val recentSearches: List<Recent> = recentSearchesDao.allRecentSearches.reversed() ?: emptyList()
        state.value = state.value.copy(
            recentSearches = recentSearches
        )
    }

    companion object {
        const val NUMBER_OF_API_CALLS = 4
    }
}
