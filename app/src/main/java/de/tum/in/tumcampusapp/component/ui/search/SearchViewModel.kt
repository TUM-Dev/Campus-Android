package de.tum.`in`.tumcampusapp.component.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.regex.Pattern
import javax.inject.Inject

typealias ApiPerson = Person

sealed class SearchResult {
    abstract val title: String

    data class Person(val person: ApiPerson) : SearchResult() {
        override val title: String
            get() = person.fullName
    }


    data class Room(val room: RoomFinderRoom) : SearchResult() {
        override val title: String
            get() = room.info
    }
}

class SearchResultsStore {

    private val _results = MutableLiveData<List<SearchResult>>()
    val results: LiveData<List<SearchResult>> = _results

    fun dispatch(searchResults: List<SearchResult>) {
        val existingSearchResults = _results.value.orEmpty()
        _results.value = existingSearchResults + searchResults
    }

    fun clear() {
        _results.value = emptyList()
    }

}

class SearchViewModel @Inject constructor(
    private val tumOnlineClient: TUMOnlineClient,
    private val tumCabeClient: TUMCabeClient
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val store = SearchResultsStore()

    val results: LiveData<List<SearchResult>> = store.results

    fun search(query: String) {
        val persons = tumOnlineClient
            .searchPersonRx(query)
            .subscribeOn(Schedulers.io())
            .map { it.persons }
            .map { persons -> persons.map { SearchResult.Person(it) } }

        val rooms = tumCabeClient
            .fetchRoomsRx(userRoomSearchMatching(query))
            .subscribeOn(Schedulers.io())
            .map { rooms -> rooms.map { SearchResult.Room(it) } }

        compositeDisposable += Single
            .merge(persons, rooms)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { store.dispatch(it) }
    }

    fun clear() {
        store.clear()
    }

    /**
     * Distinguishes between some room searches, eg. MW 2001 or MI 01.15.069 and takes the
     * number part so that the search can return (somewhat) meaningful results
     * (Temporary and non-optimal)
     *
     * @return a new query or the original one if nothing was matched
     */
    // TODO
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

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}
