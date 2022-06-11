package de.tum.`in`.tumcampusapp.component.ui.search

import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.IllegalStateException
import java.util.regex.Pattern
import javax.inject.Inject

class SearchViewModel @Inject constructor(
        private val tumOnlineClient: TUMOnlineClient,
        private val tumCabeClient: TUMCabeClient
) : ViewModel() {

    val state: MutableStateFlow<SearchResultState> = MutableStateFlow(SearchResultState())

    private val persons: MutableStateFlow<List<SearchResult>> = MutableStateFlow(emptyList())
    private val rooms: MutableStateFlow<List<SearchResult>> = MutableStateFlow(emptyList())
    private val lectures: MutableStateFlow<List<SearchResult>> = MutableStateFlow(emptyList())

    private val compositeDisposable = CompositeDisposable()

    fun changeResultType(type: SearchResultType) {
        val selectedResult: List<SearchResult> = when (type) {
            SearchResultType.PERSON -> persons.value
            SearchResultType.ROOM -> rooms.value
            SearchResultType.LECTURE -> lectures.value
            SearchResultType.ALL -> persons.value + rooms.value + lectures.value
        }
        state.value = state.value.copy(
                data = sort(selectedResult),
                selectedType = type
        )
    }

    fun search(query: String) {
        state.value = state.value.copy(
                isLoading = true,
                data = emptyList(),
                availableResultTypes = emptyList(),
                selectedType = SearchResultType.ALL
        )

        val persons = tumOnlineClient
                .searchPerson2(query)
                .subscribeOn(Schedulers.io())
                .map { it.persons }
                .doOnError(Utils::log)
                .onErrorReturn { emptyList() }
                .map { persons ->
                    persons.map { SearchResult.Person(it) }
                }

        val rooms = tumCabeClient
                .fetchRooms2(userRoomSearchMatching(query))
                .subscribeOn(Schedulers.io())
                .doOnError(Utils::log)
                .onErrorReturn { emptyList() }
                .map { rooms ->
                    rooms.map { SearchResult.Room(it) }
                }

        val lectures = tumOnlineClient
                .searchLectures2(query)
                .subscribeOn(Schedulers.io())
                .map {it.lectures }
                .doOnError(Utils::log)
                .onErrorReturn { emptyList() }
                .map { lectures ->
                    lectures.map { SearchResult.Lecture(it) }
                }

        compositeDisposable += Single
                .concat(persons, rooms, lectures)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { saveSearchResult(it) }
    }

    private fun saveSearchResult(result: List<SearchResult>) {
        if (result.isEmpty()) {
            saveResult(emptyList(), null)
            return
        }

        val type : SearchResultType = when (result[0]) {
            is SearchResult.Person -> SearchResultType.PERSON
            is SearchResult.Room -> SearchResultType.ROOM
            is SearchResult.Lecture -> SearchResultType.LECTURE
        }

        when (type) {
            SearchResultType.PERSON -> persons.value = result
            SearchResultType.ROOM -> rooms.value = result
            SearchResultType.LECTURE -> lectures.value = result
            else -> throw IllegalStateException("Not know search result type!")
        }
        saveResult(result, type)
    }

    private fun saveResult(result: List<SearchResult>, type: SearchResultType?) {
        var availableTypes = state.value.availableResultTypes
        if (type != null)
            availableTypes = availableTypes + listOf(type)
        state.value = state.value.copy(
                isLoading = false,
                data = sort(state.value.data + result),
                availableResultTypes = availableTypes
        )
    }

    /**
     * Distinguishes between some room searches, eg. MW 2001 or MI 01.15.069 and takes the
     * number part so that the search can return (somewhat) meaningful results
     * (Temporary and non-optimal)
     *
     * @return a new query or the original one if nothing was matched
     */
    private fun userRoomSearchMatching(roomSearchQuery: String): String {
        // Matches the number part if the String is composed of two words, probably wrong:

        // First group captures numbers with dots, like the 01.15.069 part from 'MI 01.15.069'
        // (This is the best search format for MI room numbers)
        // The second group captures numbers and mixed formats with letters, like 'MW2001'
        // Only the first match will be returned
        val pattern = Pattern.compile("(\\w+(?:\\.\\w+)+)|(\\w+\\d+)")

        val matcher = pattern.matcher(roomSearchQuery)

        return if (matcher.find()) {
            matcher.group()
        } else {
            roomSearchQuery
        }
    }

    private fun sort(result: List<SearchResult>): List<SearchResult> {
        return result.sortedBy { it.title }
    }
}