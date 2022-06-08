package de.tum.`in`.tumcampusapp.component.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.regex.Pattern
import javax.inject.Inject

class SearchViewModel @Inject constructor(
        private val tumOnlineClient: TUMOnlineClient,
        private val tumCabeClient: TUMCabeClient
) : ViewModel() {

    val searchResultList = MutableLiveData<List<SearchResult>>()

    private val compositeDisposable = CompositeDisposable()

    fun search(query: String) {
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
                .merge(persons, rooms, lectures)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { searchResults ->
                    searchResultList.value = searchResults
                }
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

}