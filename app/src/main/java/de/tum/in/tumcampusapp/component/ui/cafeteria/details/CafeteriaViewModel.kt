package de.tum.`in`.tumcampusapp.component.ui.cafeteria.details

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.interactors.FetchBestMatchMensaInteractor
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.utils.ErrorHelper
import de.tum.`in`.tumcampusapp.utils.LocationHelper
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * ViewModel for cafeterias.
 */
class CafeteriaViewModel @Inject constructor(
        private val bestMatchMensaInteractor: FetchBestMatchMensaInteractor,
        private val localRepository: CafeteriaLocalRepository,
        private val remoteRepository: CafeteriaRemoteRepository
) : ViewModel() {

    private val _cafeterias = MutableLiveData<List<Cafeteria>>()
    val cafeterias: LiveData<List<Cafeteria>> = _cafeterias

    private val _selectedCafeteria = MutableLiveData<Cafeteria>()
    val selectedCafeteria: LiveData<Cafeteria> = _selectedCafeteria

    private val _cafeteriaMenus = MutableLiveData<List<CafeteriaMenu>>()
    val cafeteriaMenus: LiveData<List<CafeteriaMenu>> = _cafeteriaMenus

    private val _menuDates = MutableLiveData<List<DateTime>>()
    val menuDates: LiveData<List<DateTime>> = _menuDates

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    private val compositeDisposable = CompositeDisposable()

    // TODO: More interactors

    fun fetchBestMatchMensaId(): Int {
        return bestMatchMensaInteractor.execute()
    }

    fun updateSelectedCafeteria(cafeteria: Cafeteria) {
        _selectedCafeteria.postValue(cafeteria)
    }

    fun fetchCafeterias(location: Location) {
        compositeDisposable += getAllCafeterias(location)
                .doOnError { _error.postValue(true) }
                .doOnNext { _error.postValue(it.isEmpty()) }
                .subscribe(_cafeterias::postValue, ErrorHelper::crashOnException)
    }

    fun fetchMenuDates() {
        compositeDisposable += fetchAllMenuDates()
                .subscribe(_menuDates::postValue, ErrorHelper::crashOnException)
    }

    /**
     * Returns a flowable that emits a list of cafeterias from the local repository.
     */
    private fun getAllCafeterias(location: Location): Flowable<List<Cafeteria>> {
        return localRepository.getAllCafeterias()
                .map { transformCafeteria(it, location) }
                .subscribeOn(Schedulers.io())
                .defaultIfEmpty(emptyList())
    }

    fun fetchCafeteriaMenus(id: Int, date: DateTime) {
        compositeDisposable += Flowable.fromCallable { localRepository.getCafeteriaMenus(id, date) }
                .subscribeOn(Schedulers.io())
                .defaultIfEmpty(emptyList())
                .subscribe { _cafeteriaMenus.postValue(it) }
    }

    private fun fetchAllMenuDates(): Flowable<List<DateTime>> {
        return Flowable
                .fromCallable { localRepository.getAllMenuDates() }
                .subscribeOn(Schedulers.io())
                .defaultIfEmpty(emptyList())
    }


    /**
     * Downloads cafeterias and stores them in the local repository.
     *
     * First checks whether a sync is necessary
     * Then clears current cache
     * Insert new cafeterias
     * Lastly updates last sync
     *
     */
    fun getCafeteriasFromService(force: Boolean) {
        compositeDisposable += Observable
                .fromCallable { localRepository.getLastSync() == null || force }
                .doOnNext { localRepository.clear() }
                .doAfterNext { localRepository.updateLastSync() }
                .flatMap { remoteRepository.getAllCafeterias() }
                .subscribeOn(Schedulers.io())
                .subscribe(localRepository::addCafeterias, ErrorHelper::crashOnException)
    }

    /**
     * Adds the distance between user and cafeteria to model.
     */
    private fun transformCafeteria(cafeterias: List<Cafeteria>, location: Location): List<Cafeteria> {
        return cafeterias.map {
            val distance = LocationHelper.calculateDistanceToCafeteria(it, location)
            it.copy(distance = distance)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    class Factory(
            private val bestMatchMensaInteractor: FetchBestMatchMensaInteractor,
            private val localRepository: CafeteriaLocalRepository,
            private val remoteRepository: CafeteriaRemoteRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST") // no good way around this
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CafeteriaViewModel(bestMatchMensaInteractor, localRepository, remoteRepository) as T
        }

    }

}
