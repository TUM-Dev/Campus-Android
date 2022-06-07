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
                .map { it.persons }
                .doOnError(Utils::log)
                .onErrorReturn { emptyList() }
                .subscribeOn(Schedulers.io())
                .map { persons ->
                    persons.map { SearchResult.Person(it) }
                }

        compositeDisposable += Single
                .merge(persons, persons)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { searchResults ->
                    searchResultList.value = searchResults
                }
    }

}